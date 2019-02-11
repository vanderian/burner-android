package com.vander.burner.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.vander.burner.R
import com.vander.scaffold.screen.Event
import io.reactivex.Maybe
import io.reactivex.Observable
import kotlinx.android.parcel.Parcelize
import me.dm7.barcodescanner.zxing.ZXingScannerView
import pm.gnosis.svalinn.common.utils.toast

fun Toolbar.animatedClose() = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_close)?.run {
  navigationIcon = this
  start()
}

fun ZXingScannerView.scans(): Observable<String> =
    Observable.create<String> { emitter ->
      lateinit var handler: ZXingScannerView.ResultHandler
      handler = ZXingScannerView.ResultHandler {
        emitter.onNext(it.text)
        resumeCameraPreview(handler)
      }
      setResultHandler(handler)
      startCamera()
//      postOnAnimationDelayed({ startCamera() }, 500)
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

fun Context.copyToClipboard(label: CharSequence, text: CharSequence): Boolean =
    (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let {
      it.primaryClip = ClipData.newPlainText(label, text)
      toast(R.string.label_copy_address, Toast.LENGTH_SHORT)
    } != null

@Parcelize
data class ShowDialogEvent(
    @StringRes val title: Int,
    @StringRes val content: Int = 0,
    @StringRes val positiveButton: Int = 0,
    @StringRes val negativeButton: Int = 0,
    val contentString: String? = null
) : Event, Parcelable