package com.vander.burner.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcelable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.vander.burner.R
import com.vander.scaffold.screen.Event
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.layout_receipt.view.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import pm.gnosis.crypto.utils.Sha3Utils
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.toast
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.removeHexPrefix
import timber.log.Timber
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

object Utils {
  fun createQrBitmap(content: String, size: Int): Single<Bitmap> =
      Single.create<Bitmap> { emitter ->
        try {
          val encoder = QRCodeWriter()

          val darkColor: Int = Color.BLACK
          val lightColor: Int = Color.TRANSPARENT
          val hints = hashMapOf(EncodeHintType.MARGIN to 0, EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
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

fun Toolbar.animatedClose() = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_close)?.run {
  navigationIcon = this
  start()
}

fun ZXingScannerView.scans(): Observable<String> =
    Observable.create<String> { emitter ->
      lateinit var handler: ZXingScannerView.ResultHandler
      handler = ZXingScannerView.ResultHandler {
        emitter.onNext(it.text)
        postDelayed({ resumeCameraPreview(handler) }, 3000)
      }
      setResultHandler(handler)
      startCamera()
      emitter.setCancellable { stopCamera() }
    }


fun Context.showConfirmDialog(data: ShowDialogEvent): Maybe<Unit> =
    Maybe.create<Unit> { emitter ->
      val dialogBuilder = AlertDialog.Builder(this)
          .setTitle(getString(data.title))

      if (data.content > 0) {
        dialogBuilder.setMessage(data.content)
      } else if (data.contentString != null) {
        dialogBuilder.setMessage(data.contentString)
      }

      if (data.positiveButton > 0) {
        dialogBuilder.setPositiveButton(data.positiveButton) { _, _ -> emitter.onSuccess(Unit) }
      }

      if (data.negativeButton > 0) {
        dialogBuilder.setNegativeButton(data.negativeButton) { _, _ -> emitter.onComplete() }
      }

      dialogBuilder.create().let { dialog ->
        dialog.setOnDismissListener { emitter.onComplete() }
        emitter.setCancellable { dialog.dismiss() }
        dialog.show()
      }
    }

fun Context.showReceiptDialog(event: ShowReceiptEvent) = Maybe.create<Unit> { emitter ->
  val view = LayoutInflater.from(this).inflate(R.layout.layout_receipt, null)
  AlertDialog.Builder(this)
      .setTitle(R.string.title_receipt)
      .setView(view)
      .setOnDismissListener { if (event.hasAction) emitter.onSuccess(Unit) else emitter.onComplete() }
      .apply {
        if (event.hasAction) {
          setPositiveButton(R.string.action_scan_next) { _, _ -> emitter.onComplete() }
          setNeutralButton(R.string.action_close) { _, _ -> emitter.onSuccess(Unit) }
        }
      }
      .create()
      .apply {
        emitter.setCancellable { dismiss() }
        view.imageBlockieFrom.setAddress(event.data.from)
        view.imageBlockieTo.setAddress(event.data.to)
        view.textAddressFrom.text = event.data.from.asEthereumAddressShort()
        view.textAddressTo.text = event.data.to.asEthereumAddressShort()
        view.textAmount.text = NumberFormat.getCurrencyInstance(Locale.US).format(event.amount)
      }
      .show()
}


fun Context.copyToClipboard(label: CharSequence, text: CharSequence, @StringRes toast: Int): Boolean =
    (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let {
      it.primaryClip = ClipData.newPlainText(label, text)
      toast(toast, Toast.LENGTH_SHORT)
    } != null

data class ShowDialogEvent(
    @StringRes val title: Int,
    @StringRes val content: Int = 0,
    @StringRes val positiveButton: Int = 0,
    @StringRes val negativeButton: Int = 0,
    val contentString: String? = null
) : Event

data class ShowReceiptEvent(
    val data: TransactionReceipt,
    val amount: BigDecimal,
    val hasAction: Boolean = false
) : Event

fun Solidity.Address.asEthereumAddressShort() = asEthereumAddressString().removeHexPrefix().take(6)