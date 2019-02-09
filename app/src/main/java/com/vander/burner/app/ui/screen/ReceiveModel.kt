package com.vander.burner.app.ui.screen

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.vander.burner.app.data.AccountRepository
import com.vander.scaffold.screen.GoBack
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.with
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pm.gnosis.utils.asEthereumAddressString
import timber.log.Timber
import javax.inject.Inject

class ReceiveModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ScreenModel<ReceiveState, ReceiveIntents>(ReceiveState(accountRepository.address)) {
  private val encoder = QRCodeWriter()

  override fun collectIntents(intents: ReceiveIntents, result: Observable<Result>): Disposable {

    val qr = createQrBitmap(accountRepository.address.asEthereumAddressString())
        .doOnSuccess { state.next { copy(qr = it) } }

    val copy = intents.copy()

    return CompositeDisposable().with(
        qr.subscribe(),
        intents.navigation().subscribe { event.onNext(GoBack) },
        copy.subscribe()
    )
  }

  private fun createQrBitmap(content: String, darkColor: Int = Color.BLACK, lightColor: Int = Color.TRANSPARENT, marginSize: Int = 0): Single<Bitmap> =
      Single.create<Bitmap> { emitter ->
        try {
          val size = Encoder.encode(content, ErrorCorrectionLevel.M, null).matrix.width * 4
          val hints = hashMapOf(EncodeHintType.MARGIN to marginSize, EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
          val result = encoder.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

          val width = result.width
          val height = result.height
          val pixels = IntArray(width * height)

          for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
              pixels[offset + x] = if (result.get(x, y)) darkColor else lightColor
            }
          }

          val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
          bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
          emitter.onSuccess(bitmap)
        } catch (e: Exception) {
          Timber.i(e, "Could not create qr code")
          emitter.onError(e)
        }
      }.subscribeOn(Schedulers.computation())


}