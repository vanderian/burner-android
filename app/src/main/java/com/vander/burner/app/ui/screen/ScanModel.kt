package com.vander.burner.app.ui.screen

import android.net.Uri
import com.vander.scaffold.event
import com.vander.scaffold.screen.Empty
import com.vander.scaffold.screen.PopStack
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.utils.asEthereumAddress
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScanModel @Inject constructor(
) : ScreenModel<Empty, ScanIntents>() {
  override fun collectIntents(intents: ScanIntents, result: Observable<Result>): Disposable {

    val address = intents.scan()
        .publish { scan ->
          Observable.amb(listOf(
              scan.filter { it.asEthereumAddress() != null }
                  .map { TransferData(it) }
                  .doAfterNext { event.onNext(ScanScreenDirections.actionScanScreenToSendScreen(it).event()) },
              scan.filter { Uri.parse(it).host != null }
                  .map { it.substringAfterLast("/").split(";") }
                  .filter { it[0].asEthereumAddress() != null }
                  .map { TransferData(it[0], it.getOrElse(1) { "" }, it.getOrElse(2) { "" }) }
                  .doAfterNext { event.onNext(ScanScreenDirections.actionScanScreenToSendScreen(it).event()) },
              scan.delaySubscription(300, TimeUnit.MILLISECONDS)
                  .doAfterNext { event.onNext(UnknownQrCode(it)) }
          ))
        }

    return CompositeDisposable().with(
        address.subscribe(),
        intents.navigation().subscribe { event.onNext(PopStack) }
    )
  }
}