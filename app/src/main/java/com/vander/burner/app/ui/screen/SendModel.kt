package com.vander.burner.app.ui.screen

import com.vander.burner.R
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.net.XdaiProvider
import com.vander.burner.app.net.safeApiCall
import com.vander.burner.app.validator.AddressRule
import com.vander.burner.app.validator.GreaterThenZeroRule
import com.vander.burner.app.validator.NotEmptyRule
import com.vander.scaffold.debug.log
import com.vander.scaffold.event
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.PopStack
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.utils.addHexPrefix
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.toHex
import pm.gnosis.utils.toHexString
import javax.inject.Inject

class SendModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val xdaiProvider: XdaiProvider
) : ScreenModel<SendState, SendIntents>(SendState()) {

  val form = Form().withInputValidations(
      Validation(R.id.inputAddress, NotEmptyRule, AddressRule(accountRepository.address)),
      Validation(R.id.inputAmount, NotEmptyRule, GreaterThenZeroRule) //max balance rule
  )

  override fun collectIntents(intents: SendIntents, result: Observable<Result>): Disposable {

    val submit = intents.send()
        .filter { form.validate(event) }
        .flatMapMaybe {
          xdaiProvider.transfer(
              form.inputText(R.id.inputAddress),
              form.inputText(R.id.inputAmount),
              form.inputText(R.id.inputMessage)
          ).safeApiCall(event)
        }
        .doOnNext { event.onNext(PopStack) }

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