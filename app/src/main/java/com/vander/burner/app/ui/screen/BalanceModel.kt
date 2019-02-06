package com.vander.burner.app.ui.screen

import com.vander.burner.app.net.Web3Provider
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BalanceModel @Inject constructor(
    private val web3Provider: Web3Provider
) : ScreenModel<BalanceState, BalanceIntents>(BalanceState()) {

  override fun collectIntents(intents: BalanceIntents, result: Observable<Result>): Disposable {

    return CompositeDisposable().with(
        Observable.interval(3, TimeUnit.SECONDS)
            .flatMapSingle {
              web3Provider.xdaiBalance
                  .doOnSuccess { state.next { copy(balance = it) } }
            }
            .subscribe()
    )
  }
}