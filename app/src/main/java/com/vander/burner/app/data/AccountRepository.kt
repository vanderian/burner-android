package com.vander.burner.app.data

import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.vander.burner.R
import com.vander.burner.app.validator.MnemonicRule
import com.vander.burner.app.validator.PrivateKeyRule
import com.vander.scaffold.annotations.ApplicationScope
import de.adorsys.android.securestoragelibrary.SecurePreferences
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okio.ByteString
import pm.gnosis.crypto.KeyGenerator
import pm.gnosis.crypto.KeyPair
import pm.gnosis.mnemonic.Bip39
import pm.gnosis.model.Solidity
import pm.gnosis.utils.*
import javax.inject.Inject

@ApplicationScope
class AccountRepository @Inject constructor(
    private val rxPrefs: RxSharedPreferences,
    private val bip39: Bip39
) {

  private val addressPref = rxPrefs.getString("user.account.address")

  val hasCredentials: Boolean
    get() = addressPref.isSet && SecurePreferences.contains(addressPref.get())

  val keyPair: KeyPair
    get() {
      check(hasCredentials)
      return with(addressPref.get()) { KeyPair.fromPrivate(SecurePreferences.getStringValue(this, null)!!.hexAsBigInteger()) }
    }

  val keyPairAsync: Single<KeyPair> = Single.fromCallable { keyPair }.subscribeOn(Schedulers.computation())

  val address: Solidity.Address
    get() {
      check(hasCredentials)
      return addressPref.get().asEthereumAddress()!!
    }

  private fun save(keyPair: KeyPair) {
    SecurePreferences.clearAllValues()
    val address = Solidity.Address(keyPair.address.asBigInteger()).asEthereumAddressString()
    addressPref.set(address)
    SecurePreferences.setValue(address, keyPair.privKey.toHexString())
  }

  fun import(keyPair: KeyPair): Completable = burn()
      .doOnComplete { save(keyPair) }

  fun generatePk(input: String) = Single.fromCallable {
    when {
      PrivateKeyRule.validate(input) -> KeyPair.fromPrivate(input.hexAsBigInteger())
      MnemonicRule(bip39).validate(input) -> bip39.mnemonicToSeed(input).let {
        KeyGenerator.masterNode(ByteString.of(*it)).derive(KeyGenerator.BIP44_PATH_ETHEREUM).deriveChild(0).keyPair
      }
      else -> throw IllegalArgumentException("not a valid private key or mnemonic seed: $input")
    }
  }.subscribeOn(Schedulers.computation())

  fun createAccount(): Completable =
      if (hasCredentials) Completable.complete()
      else Single.fromCallable { bip39.generateMnemonic(languageId = R.id.english) }
          .flatMap { generatePk(it) }
          .doOnSuccess { save(it) }
          .ignoreElement()
          .subscribeOn(Schedulers.computation())

  //  delete keystore, prefs, local db, keep persistent prefs
  fun burn(): Completable = Completable.fromCallable {
    SecurePreferences.clearAllValues()
    rxPrefs.clear()
  }
}