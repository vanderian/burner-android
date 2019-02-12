package com.vander.burner.app.ui.screen

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.tbruyelle.rxpermissions2.RxPermissions
import com.vander.burner.R
import com.vander.burner.app.ui.animatedClose
import com.vander.burner.app.ui.scans
import com.vander.scaffold.screen.Empty
import com.vander.scaffold.screen.Screen
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.screen_scan.*
import pm.gnosis.svalinn.common.utils.toast

class ScanScreen : Screen<Empty, ScanIntents>() {
  override fun layout(): Int = R.layout.screen_scan

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.animatedClose()
  }

  override fun intents(): ScanIntents = object : ScanIntents {
    override val toolbar: Toolbar
      get() = this@ScanScreen.toolbar

    override fun scan(): Observable<String> = RxPermissions(requireActivity()).request(Manifest.permission.CAMERA)
        .filter { it }
        .flatMap { viewScan.scans() }

    override fun events(): List<Observable<*>> = listOf(
        event(UnknownQrCode::class).doOnNext { requireContext().toast(getString(R.string.error_app_qr, it.value)) }
    )
  }

  override fun render(state: Empty) {

  }
}