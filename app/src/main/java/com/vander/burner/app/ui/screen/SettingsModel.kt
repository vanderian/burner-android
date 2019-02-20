package com.vander.burner.app.ui.screen

import com.f2prateek.rx.preferences2.Preference
import com.vander.burner.BuildConfig
import com.vander.burner.R
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.di.ScreenSize
import com.vander.burner.app.net.XdaiProvider
import com.vander.burner.app.net.errorHandlingCall
import com.vander.burner.app.net.loadingCall
import com.vander.burner.app.ui.ShowDialogEvent
import com.vander.burner.app.ui.Utils
import com.vander.burner.app.validator.MnemonicRule
import com.vander.burner.app.validator.NotEmptyRule
import com.vander.burner.app.validator.PrivateKeyRule
import com.vander.scaffold.event
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.PopStack
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.mnemonic.Bip39
import pm.gnosis.model.Solidity
import pm.gnosis.utils.toHexString
import javax.inject.Inject

class SettingsModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val xdaiProvider: XdaiProvider,
    private val paired: Preference<Solidity.Address>,
    @ScreenSize private val size: Int,
    private val bip39: Bip39
) : ScreenModel<SettingsState, SettingsIntents>() {

  val form = Form(event).withInputValidations(
      Validation(R.id.inputPrivate, NotEmptyRule, PrivateKeyRule),
      Validation(R.id.inputSeed, NotEmptyRule, MnemonicRule(bip39))
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
              .toSingleDefault(Unit).loadingCall(event).toMaybe()
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

    val createPrivate = intents.createFromKey()
        .filter { form.validate() }
        .map { form.inputText(R.id.inputPrivate) }

    val createSeed = intents.createFromSeed()
        .filter { form.validate() }
        .map { form.inputText(R.id.inputSeed) }

    val createOrWithdraw = Observable.merge(createPrivate, createSeed)
        .flatMapMaybe { input ->
          xdaiProvider.isEmptyAccount().map {
            if (it) SettingsScreenDirections.actionSettingsScreenToInitScreen(input).event()
            else SettingsScreenDirections.actionSettingsScreenToWithdrawScreen(input).event()
          }.errorHandlingCall(event)
        }
        .doOnNext { event.onNext(it) }

    val beer = intents.beer()
        .doOnNext {
          event.onNext(SettingsScreenDirections.actionSettingsScreenToSendScreen(
              TransferData(BuildConfig.UNSPENT_SINK, "5", "Hey, have a beer on me!")).event()
          )
        }

    val pair = intents.pair()
        .doOnNext { event.onNext(SettingsScreenDirections.actionSettingsScreenToPairScreen().event()) }

    val balance = xdaiProvider.isEmptyAccount(event)
        .doOnNext { state.onNext(SettingsState(it)) }

    return CompositeDisposable().with(
        form.subscribe(intents),
        intents.focusChanges().subscribe(),
        intents.show().subscribe(),
        pair.subscribe(),
        burn.subscribe(),
        qr.subscribe(),
        copy.subscribe(),
        createOrWithdraw.subscribe(),
        beer.subscribe(),
        balance.subscribe(),
        intents.navigation().subscribe { event.onNext(PopStack) }
    )
  }
}
