package com.vander.burner.app.ui.screen

import com.f2prateek.rx.preferences2.Preference
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.net.XdaiProvider
import com.vander.burner.app.net.safeApiCall
import com.vander.scaffold.event
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.model.Solidity
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BalanceModel @Inject constructor(
    private val xdaiProvider: XdaiProvider,
    accountRepository: AccountRepository,
    paired: Preference<Solidity.Address>
) : ScreenModel<BalanceState, BalanceIntents>(BalanceState(accountRepository.address, paired.isSet)) {

  override fun collectIntents(intents: BalanceIntents, result: Observable<Result>): Disposable {

    val receive = intents.receive()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToReceiveScreen().event()) }

    val send = intents.send()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToSendScreen(null).event()) }

    val scan = intents.scan()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToScanScreen().event()) }

    val balance = Observable.interval(3, TimeUnit.SECONDS).startWith(0)
        .flatMapMaybe { xdaiProvider.balance.doOnSuccess { state.next { copy(balance = it.toEther()) } }.safeApiCall(event) }

    val settings = intents.settings()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToSettingsScreen().event()) }

    val pair = intents.pair()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToPairScreen().event()) }

    return CompositeDisposable().with(
        pair.subscribe(),
        balance.subscribe(),
        receive.subscribe(),
        send.subscribe(),
        scan.subscribe(),
        settings.subscribe()
    )
  }
}