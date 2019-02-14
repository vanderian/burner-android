package com.vander.burner.app.ui.screen

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.appcompat.itemClicks
import com.jakewharton.rxbinding3.view.clicks
import com.vander.burner.R
import com.vander.burner.app.net.apiHandler
import com.vander.burner.app.ui.asEthereumAddressShort
import com.vander.scaffold.screen.Screen
import com.vander.scaffold.ui.visibility
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.screen_balance.*
import java.lang.RuntimeException
import java.text.NumberFormat
import java.util.*

class BalanceScreen : Screen<BalanceState, BalanceIntents>() {
  override fun layout(): Int = R.layout.screen_balance

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.inflateMenu(R.menu.settings)
  }

  override fun intents(): BalanceIntents = object : BalanceIntents {
    override fun pair(): Observable<Unit> = buttonPair.clicks()
    override fun receive(): Observable<Unit> = buttonReceive.clicks()
    override fun send(): Observable<Unit> = buttonSend.clicks()
    override fun scan(): Observable<Unit> = fab.clicks()
    override fun settings(): Observable<Unit> = toolbar.itemClicks().map { Unit }
    override fun explorerAddress(): Observable<Unit> = Observable.merge(imageBlockie.clicks(), textAddressShort.clicks())
    override fun explorerPaired(): Observable<Unit> = Observable.merge(imageBlockiePaired.clicks(), textAddressPaired.clicks())
    override fun events(): List<Observable<*>> = apiHandler { /*layoutLoading.toggle(it)*/ }
  }

  override fun render(state: BalanceState) {
    textBalance.text = NumberFormat.getCurrencyInstance(Locale.US).format(state.balance)
    imageBlockie.setAddress(state.address)
    textAddressShort.text = state.address.asEthereumAddressShort()
    groupPaired.visibility = (state.pairedAddress != null).visibility()
    buttonPair.visibility = (state.pairedAddress == null).visibility()
    state.pairedAddress?.let {
      imageBlockiePaired.setAddress(it)
      textAddressPaired.text = state.pairedAddress.asEthereumAddressShort()
    }
  }
}