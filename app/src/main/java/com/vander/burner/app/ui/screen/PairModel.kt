package com.vander.burner.app.ui.screen

import com.f2prateek.rx.preferences2.Preference
import com.vander.burner.R
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.ui.asEthereumAddressShort
import com.vander.burner.app.validator.AddressEnsRule
import com.vander.burner.app.validator.NotEmptyRule
import com.vander.scaffold.event
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.PopStack
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.screen.ToastEvent
import com.vander.scaffold.ui.with
import com.vander.scaffold.unbundle
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import javax.inject.Inject

class PairModel @Inject constructor(
    private val paired: Preference<Solidity.Address>,
    accountRepository: AccountRepository
) : ScreenModel<PairState, PairIntents>(PairState()) {

  private val form = Form(event).withInputValidations(Validation(R.id.inputAddress, NotEmptyRule, AddressEnsRule(accountRepository.address)))

  override fun collectIntents(intents: PairIntents, result: Observable<Result>): Disposable {
    val pairedAddress = paired.asObservable()
        .map { if (paired.isSet) paired.get().asEthereumAddressString() else "" }
        .doOnNext { state.onNext(PairState(it)) }

    val clear = intents.clear()
        .doOnNext { paired.delete() }

    val pair = intents.pair()
        .filter { form.validate() }
        .doOnNext { paired.set(form.inputText(R.id.inputAddress).asEthereumAddress()!!) }
        .doOnNext { event.onNext(ToastEvent(R.string.result_paired)) }
        .doOnNext { event.onNext(PopStack) }

    val scan = intents.scan()
        .doOnNext { event.onNext(PairScreenDirections.actionSendScreenToScanScreenForResult().event()) }

    val scanned = result
        .filter { it.success && it.request == R.id.scanScreenForResult }
        .doOnNext { event.onNext(it.extras!!.unbundle<TransferData>()) }

    return CompositeDisposable().with(
        form.subscribe(intents),
        pairedAddress.subscribe(),
        clear.subscribe(),
        pair.subscribe(),
        scan.subscribe(),
        scanned.subscribe(),
        intents.navigation().subscribe { event.onNext(PopStack) }
    )
  }

}