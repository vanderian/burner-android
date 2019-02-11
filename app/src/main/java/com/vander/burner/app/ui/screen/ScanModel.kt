package com.vander.burner.app.ui.screen

import com.vander.scaffold.event
import com.vander.scaffold.screen.Empty
import com.vander.scaffold.screen.PopStack
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ScanModel @Inject constructor(

) : ScreenModel<Empty, ScanIntents>() {
  override fun collectIntents(intents: ScanIntents, result: Observable<Result>): Disposable {

    val address = intents.address()
        .doOnNext { event.onNext(ScanScreenDirections.actionScanScreenToSendScreen(it).event()) }

    return CompositeDisposable().with(
        address.subscribe(),
        intents.navigation().subscribe { event.onNext(PopStack) }
    )
  }
}