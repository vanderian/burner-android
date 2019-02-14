package com.vander.burner.app.ui.screen

import com.f2prateek.rx.preferences2.Preference
import com.vander.burner.BuildConfig
import com.vander.burner.R
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.di.ScreenSize
import com.vander.burner.app.net.XdaiProvider
import com.vander.burner.app.ui.ShowDialogEvent
import com.vander.burner.app.ui.Utils
import com.vander.burner.app.validator.NotEmptyRule
import com.vander.burner.app.validator.PrivateKeyRule
import com.vander.burner.app.validator.SeedRule
import com.vander.scaffold.event
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.Empty
import com.vander.scaffold.screen.PopStack
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.crypto.KeyPair
import pm.gnosis.mnemonic.Bip39
import pm.gnosis.model.Solidity
import pm.gnosis.utils.hexAsBigInteger
import pm.gnosis.utils.toHexString
import javax.inject.Inject

class SettingsModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val xdaiProvider: XdaiProvider,
    private val paired: Preference<Solidity.Address>,
    @ScreenSize private val size: Int,
    private val bip39: Bip39
) : ScreenModel<Empty, SettingsIntents>() {

  val form = Form().withInputValidations(
      Validation(R.id.inputPrivate, NotEmptyRule, PrivateKeyRule),
      Validation(R.id.inputSeed, NotEmptyRule, SeedRule(bip39))
  )

  override fun collectIntents(intents: SettingsIntents, result: Observable<Result>): Disposable {

    val pk = accountRepository.keyPairAsync.map { it.privKey.toHexString() }
        .cache()

    val qr = pk.flatMap { Utils.createQrBitmap(it, size) }
        .doOnSuccess { event.onNext(ShowKeyEvent(it)) }

    val burn = intents.burn()
        .flatMapMaybe {
          intents.burnConfirm(
              ShowDialogEvent(
                  R.string.dialog_burn_title,
                  if (paired.isSet) R.string.dialog_burn_message_paired
                  else R.string.dialog_burn_message,
                  R.string.action_burn_it,
                  R.string.action_cancel))
        }
        .flatMapMaybe {
          xdaiProvider.burn()
              .toMaybe<Unit>()
              .observeOn(AndroidSchedulers.mainThread())
              .onErrorResumeNext(intents.burnConfirm(
                  ShowDialogEvent(
                      R.string.dialog_burn_title,
                      R.string.dialog_burn_message_error,
                      R.string.action_burn_it,
                      R.string.action_cancel))
              )
        }
        .flatMapCompletable {
          accountRepository.burn()
              .doOnComplete { event.onNext(SettingsScreenDirections.actionSettingsScreenToInitScreen().event()) }
        }

    val copy = intents.copy()
        .flatMapSingle { pk }
        .doOnNext { event.onNext(ClipboardEvent(it)) }

    // todo withdraw or import
    val createPrivate = intents.createFromKey()
        .filter { form.validate(event) }
        .map { KeyPair.fromPrivate(form.inputText(R.id.inputPrivate).hexAsBigInteger()) }

    // todo withdraw or import
    val createSeed = intents.createFromSeed()
        .filter { form.validate(event) }
        .map { accountRepository.generetePk(bip39.mnemonicToSeed(form.inputText(R.id.inputSeed))) }

    val beer = intents.beer()
        .doOnNext {
          event.onNext(SettingsScreenDirections.actionSettingsScreenToSendScreen(
              TransferData(BuildConfig.UNSPENT_SINK, "5", "Hey, have a beer on me!")).event()
          )
        }

    return CompositeDisposable().with(
        form.subscribe(intents, event),
        intents.focusChanges().subscribe(),
        intents.show().subscribe(),
        burn.subscribe(),
        qr.subscribe(),
        copy.subscribe(),
        createPrivate.subscribe(),
        createSeed.subscribe(),
        beer.subscribe(),
        intents.navigation().subscribe { event.onNext(PopStack) }
    )
  }
}
