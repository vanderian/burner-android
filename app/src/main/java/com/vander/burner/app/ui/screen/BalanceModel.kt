package com.vander.burner.app.ui.screen

import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.net.XdaiProvider
import com.vander.scaffold.event
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BalanceModel @Inject constructor(
    private val xdaiProvider: XdaiProvider,
    private val accountRepository: AccountRepository
) : ScreenModel<BalanceState, BalanceIntents>(BalanceState(accountRepository.address)) {

  override fun collectIntents(intents: BalanceIntents, result: Observable<Result>): Disposable {

    val burn = intents.burn()
        .flatMapCompletable {
          accountRepository.burn()
              .doOnComplete { event.onNext(BalanceScreenDirections.actionBalanceScreenToInitScreen().event()) }
        }

    val balance = Observable.interval(3, TimeUnit.SECONDS)
        .flatMapSingle { xdaiProvider.balance.doOnSuccess { state.next { copy(balance = it) } } }

    return CompositeDisposable().with(
        balance.subscribe(),
        burn.subscribe()
    )
  }
}