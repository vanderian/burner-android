package com.vander.burner.app.ui.screen

import android.net.Uri
import com.vander.burner.BuildConfig
import com.vander.burner.R
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.net.XdaiProvider
import com.vander.burner.app.net.errorHandlingCall
import com.vander.scaffold.bundle
import com.vander.scaffold.event
import com.vander.scaffold.screen.*
import com.vander.scaffold.ui.with
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import pm.gnosis.utils.asEthereumAddress
import javax.inject.Inject

class ScanModel @Inject constructor(
    private val xdaiProvider: XdaiProvider
) : ScreenModel<Empty, ScanIntents>() {
  val private = Regex(Regex.escape(BuildConfig.CLIENT_URL) + "#(0x[abcdefABCDEF1234567890]{64})")
  val request = Regex(Regex.escape(BuildConfig.CLIENT_URL) + "(0x[abcdefABCDEF1234567890]{40});?(\\d+)?;?(.+)?")

  private fun TransferData.toEvent(isForResult: Boolean) = Maybe.just(
      if (isForResult) PopWithResult(this.bundle()) else ScanScreenDirections.actionScanScreenToSendScreen(this).event()
  )

  private fun MatchResult.getOrEmpty(idx: Int) = groupValues.getOrElse(idx) { "" }

  override fun collectIntents(intents: ScanIntents, result: Observable<Result>): Disposable {
    val isResult = Screen.destinationId(args) == R.id.scanScreenForResult

    val createPrivate = { input: String ->
      xdaiProvider.isEmptyAccount().map {
        if (it) ScanScreenDirections.actionScanScreenToInitScreen(input).event()
        else ScanScreenDirections.actionScanScreenToWithdrawScreen(input).event()
      }.errorHandlingCall(event)
    }

    val address = intents.scan()
        .flatMapMaybe { input ->
          when {
            input.asEthereumAddress() != null -> TransferData(input).toEvent(isResult)
            request.matches(input) -> request.matchEntire(input)!!.let {
              TransferData(it.groupValues[1], it.getOrEmpty(2), Uri.decode(it.getOrEmpty(3))).toEvent(isResult)
            }
            private.matches(input) -> createPrivate(private.matchEntire(input)!!.groupValues[1])
            else -> Maybe.just(UnknownQrCode(input))
          }
        }
        .doOnNext { event.onNext(it) }

    return CompositeDisposable().with(
        address.subscribe(),
        intents.navigation().subscribe { event.onNext(PopStack) }
    )
  }
}