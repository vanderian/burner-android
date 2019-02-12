package com.vander.burner.app.ui.screen

import com.vander.burner.R
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.net.XdaiProvider
import com.vander.burner.app.net.safeApiCall
import com.vander.burner.app.validator.AddressRule
import com.vander.burner.app.validator.GreaterThenZeroRule
import com.vander.burner.app.validator.NotEmptyRule
import com.vander.scaffold.event
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.PopStack
import com.vander.scaffold.screen.PopStackTo
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.models.Wei
import javax.inject.Inject

class SendModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val xdaiProvider: XdaiProvider
) : ScreenModel<SendState, SendIntents>(SendState()) {

  val form = Form().withInputValidations(
      Validation(R.id.inputAddress, NotEmptyRule, AddressRule(accountRepository.address)), //can send to own address ? why ?
      Validation(R.id.inputAmount, NotEmptyRule, GreaterThenZeroRule) //max balance rule
  )

  override fun collectIntents(intents: SendIntents, result: Observable<Result>): Disposable {
    val fromScan = SendScreenArgs.fromBundle(args).transferData != null

    val submit = intents.send()
        .filter { form.validate(event) }
        .flatMapMaybe {
          xdaiProvider.createTrx(
              form.inputText(R.id.inputAddress),
              form.inputText(R.id.inputAmount),
              form.inputText(R.id.inputMessage)
          )
              .flatMap { trx -> xdaiProvider.transfer(trx).map { trx to it } }
              .safeApiCall(event)
        }
        .flatMapMaybe { (trx, hash) -> xdaiProvider.receipt(hash).safeApiCall(event).map { trx to it } }
        .observeOn(AndroidSchedulers.mainThread())
        .flatMapMaybe { (t, r) ->
          intents.receipt(r, Wei(t.value).toEther(), fromScan)
              .doOnSuccess { event.onNext(PopStackTo(R.id.balanceScreen, false)) }
              .doOnComplete { event.onNext(PopStack) }
        }

    val scan = intents.scan()
        .doOnNext { event.onNext(SendScreenDirections.actionSendScreenToScanScreen().event()) }

    return CompositeDisposable().with(
        form.subscribe(intents, event),
        submit.subscribe(),
        scan.subscribe(),
        intents.navigation().subscribe { event.onNext(PopStack) }
    )
  }

}