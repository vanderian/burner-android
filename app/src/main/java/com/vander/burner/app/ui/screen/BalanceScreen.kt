package com.vander.burner.app.ui.screen

import com.jakewharton.rxbinding3.view.clicks
import com.vander.burner.R
import com.vander.burner.app.net.apiHandler
import com.vander.burner.app.ui.asEthereumAddressShort
import com.vander.scaffold.screen.Screen
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_balance.*
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.removeHexPrefix
import java.text.NumberFormat
import java.util.*

class BalanceScreen : Screen<BalanceState, BalanceIntents>() {
  override fun layout(): Int = R.layout.screen_balance

  override fun intents(): BalanceIntents = object : BalanceIntents {
    override fun burn(): Observable<Unit> = buttonBurn.clicks()
    override fun receive(): Observable<Unit> = buttonReceive.clicks()
    override fun send(): Observable<Unit> = buttonSend.clicks()
    override fun scan(): Observable<Unit> = fab.clicks()
    override fun events(): List<Observable<*>> = apiHandler { }
  }

  override fun render(state: BalanceState) {
    textBalance.text = NumberFormat.getCurrencyInstance(Locale.US).format(state.balance)
    imageBlockie.setAddress(state.address)
    textAddressShort.text = state.address.asEthereumAddressShort()
  }
}