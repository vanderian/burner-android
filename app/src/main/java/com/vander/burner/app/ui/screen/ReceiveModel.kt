package com.vander.burner.app.ui.screen

import android.net.Uri
import com.vander.burner.BuildConfig
import com.vander.burner.R
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.di.ScreenSize
import com.vander.burner.app.ui.Utils
import com.vander.burner.app.validator.NotEmptyRule
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.PopStack
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import pm.gnosis.utils.asEthereumAddressString
import javax.inject.Inject

class ReceiveModel @Inject constructor(
    private val accountRepository: AccountRepository,
    @ScreenSize private val size: Int
) : ScreenModel<ReceiveState, ReceiveIntents>(ReceiveState(accountRepository.address.asEthereumAddressString())) {
  private val form = Form(event).withInputValidations(Validation(R.id.inputAmount, NotEmptyRule))

  override fun collectIntents(intents: ReceiveIntents, result: Observable<Result>): Disposable {

    val address = accountRepository.address.asEthereumAddressString()

    val copy = intents.copy()

    val toggle = intents.toggle()
        .doOnNext { state.next { copy(showValues = showValues.not()) } }
        .share()

    val qr = Observable.merge(
        Observable.combineLatest(
            intents.inputTextChanges(R.id.inputAmount).startWith(""),
            intents.inputTextChanges(R.id.inputMessage).startWith(""),
            BiFunction { amount: String, message: String -> TransferData(address, amount, message) }
        )
            .filter { stateValue.showValues }
            .filter { form.validate() }
            .map(this::encode),
        toggle.startWith(Unit).filter { stateValue.showValues.not() }.map { address }
    )
        .flatMapSingle { input -> Utils.createQrBitmap(input, size).doOnSuccess { state.next { copy(qr = it, qrString = input) } } }

    return CompositeDisposable().with(
        form.subscribe(intents),
        qr.subscribe(),
        intents.navigation().subscribe { event.onNext(PopStack) },
        copy.subscribe()
    )
  }

  private fun encode(td: TransferData): String =
      "${BuildConfig.CLIENT_URL}${td.address};${td.amount}${if (td.message.isBlank()) "" else ";${Uri.encode(td.message)}"}"

}