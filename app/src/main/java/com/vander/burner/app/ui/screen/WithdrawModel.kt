package com.vander.burner.app.ui.screen

import com.f2prateek.rx.preferences2.Preference
import com.vander.burner.R
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.net.XdaiProvider
import com.vander.burner.app.net.loadingCall
import com.vander.burner.app.net.safeApiCall
import com.vander.burner.app.ui.ShowReceiptEvent
import com.vander.burner.app.validator.GreaterThenZeroRule
import com.vander.burner.app.validator.MaxBalanceRule
import com.vander.burner.app.validator.NotEmptyRule
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.PopStack
import com.vander.scaffold.screen.PopStackTo
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.mnemonic.Bip39
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.asBigInteger
import javax.inject.Inject

class WithdrawModel @Inject constructor(
    private val bip39: Bip39,
    private val paired: Preference<Solidity.Address>,
    private val xdaiProvider: XdaiProvider,
    private val accountRepository: AccountRepository
) : ScreenModel<WithdrawState, WithdrawIntents>() {
  private val form = Form(event)
      .withInputValidations(Validation(R.id.inputAmount, NotEmptyRule, GreaterThenZeroRule))

  override fun collectIntents(intents: WithdrawIntents, result: Observable<Result>): Disposable {
    val keys = WithdrawScreenArgs.fromBundle(args).keys

    val keyPair = accountRepository.generatePk(keys)
        .loadingCall(event)
        .cache()

    val address =
        (if (keys.isBlank()) Single.just(paired.get())
        else keyPair.map { Solidity.Address(it.address.asBigInteger()) })
            .doOnSuccess { state.init(WithdrawState(it)) }
            .cache()

    val balance = address.flatMapObservable { xdaiProvider.balance(event, it) }
        .doOnNext { state.next { copy(balance = it) } }

    val withdraw = intents.withdraw()
        .filter { keys.isNotBlank() }
        .filter { form.validate(Validation(R.id.inputAmount, MaxBalanceRule(stateValue.balance))) }
        .map { Transaction(accountRepository.address, Wei.ether(form.inputText(R.id.inputAmount))) }
        .flatMapMaybe { xdaiProvider.transfer(it, keyPair.blockingGet(), address.blockingGet()).safeApiCall(event) }
        .observeOn(AndroidSchedulers.mainThread())
        .flatMapMaybe { (t, r) ->
          intents.receipt(ShowReceiptEvent(r, t.value!!.toEther()))
              .doOnComplete { event.onNext(PopStackTo(R.id.balanceScreen, false)) }
        }

    return CompositeDisposable().with(
        form.subscribe(intents),
        balance.subscribe(),
        withdraw.subscribe(),
        intents.navigation().subscribe { event.onNext(PopStack) }
    )
  }
}