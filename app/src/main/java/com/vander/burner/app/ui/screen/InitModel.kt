package com.vander.burner.app.ui.screen

import com.vander.burner.app.data.AccountRepository
import com.vander.scaffold.event
import com.vander.scaffold.screen.Empty
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InitModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ScreenModel<Empty, Empty>() {

  override fun collectIntents(intents: Empty, result: Observable<Result>): Disposable {

    return CompositeDisposable().with(
//        Single.timer(5, TimeUnit.SECONDS)
//            .subscribe(),
        accountRepository.createAccount()
            .doOnComplete { event.onNext(InitScreenDirections.actionInitScreenToBalanceScreen().event()) }
            .subscribe()
    )
  }
}