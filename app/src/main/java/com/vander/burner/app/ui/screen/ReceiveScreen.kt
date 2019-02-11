package com.vander.burner.app.ui.screen

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.vander.burner.R
import com.vander.burner.app.ui.animatedClose
import com.vander.burner.app.ui.copyToClipboard
import com.vander.scaffold.screen.Screen
import com.vander.scaffold.ui.visible
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.screen_receive.*
import me.dm7.barcodescanner.core.DisplayUtils
import pm.gnosis.utils.asEthereumAddressString
import kotlin.math.min

class ReceiveScreen : Screen<ReceiveState, ReceiveIntents>() {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setTitle(R.string.title_receive)
    toolbar.animatedClose()
  }

  override fun layout(): Int = R.layout.screen_receive

  override fun intents(): ReceiveIntents = object : ReceiveIntents {
    override val size: Int
      get() = DisplayUtils.getScreenResolution(requireContext()).run { min(x, y) }

    override val toolbar: Toolbar
      get() = this@ReceiveScreen.toolbar

    override fun copy(): Observable<Unit> = buttonAddress.clicks()
        .doOnNext { requireContext().copyToClipboard("public key", buttonAddress.text.toString()) }
  }

  override fun render(state: ReceiveState) {
    buttonAddress.text = state.address.asEthereumAddressString()
    state.qr?.let {
      imageQr.setImageBitmap(it)
      imageQr.visible()
    }
  }
}