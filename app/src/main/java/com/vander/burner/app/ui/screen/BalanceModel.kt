package com.vander.burner.app.ui.screen

import android.content.Intent
import com.f2prateek.rx.preferences2.Preference
import com.vander.burner.BuildConfig
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.net.XdaiProvider
import com.vander.burner.app.net.errorHandlingCall
import com.vander.scaffold.event
import com.vander.scaffold.screen.NextActivity
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.crypto.utils.asEthereumAddressChecksumString
import pm.gnosis.model.Solidity
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BalanceModel @Inject constructor(
    private val xdaiProvider: XdaiProvider,
    accountRepository: AccountRepository,
    private val paired: Preference<Solidity.Address>
) : ScreenModel<BalanceState, BalanceIntents>(BalanceState(accountRepository.address)) {

  override fun collectIntents(intents: BalanceIntents, result: Observable<Result>): Disposable {

    val pairedAddress = paired.asObservable()
        .doOnNext { state.next { copy(pairedAddress = if (paired.isSet) paired.get() else null) } }

    val receive = intents.receive()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToReceiveScreen().event()) }

    val send = intents.send()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToSendScreen(null).event()) }

    val scan = intents.scan()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToScanScreen().event()) }

    val balance = Observable.interval(XdaiProvider.BLOCK_TIME, TimeUnit.SECONDS).startWith(0)
        .flatMapMaybe { xdaiProvider.balance.doOnSuccess { state.next { copy(balance = it.toEther()) } }.errorHandlingCall(event) }

    val settings = intents.settings()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToSettingsScreen().event()) }

    val pair = intents.pair()
        .doOnNext { event.onNext(BalanceScreenDirections.actionBalanceScreenToPairScreen().event()) }

    val explorer = Observable.merge(
        intents.explorerAddress().map { stateValue.address.asEthereumAddressChecksumString() },
        intents.explorerPaired().map { paired.get().asEthereumAddressChecksumString() }
    )
        .map { NextActivity(Intent.parseUri("${BuildConfig.EXLPORER_ADDRESS_URL}$it", 0)) }
        .doOnNext { event.onNext(it) }

    return CompositeDisposable().with(
        explorer.subscribe(),
        pair.subscribe(),
        pairedAddress.subscribe(),
        balance.subscribe(),
        receive.subscribe(),
        send.subscribe(),
        scan.subscribe(),
        settings.subscribe()
    )
  }
}