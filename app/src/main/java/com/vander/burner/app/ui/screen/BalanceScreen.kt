package com.vander.burner.app.ui.screen

import com.vander.burner.R
import com.vander.scaffold.screen.Screen
import kotlinx.android.synthetic.main.screen_balance.*

class BalanceScreen : Screen<BalanceState, BalanceIntents>() {
  override fun layout(): Int = R.layout.screen_balance

  override fun intents(): BalanceIntents = object : BalanceIntents {}

  override fun render(state: BalanceState) {
    textBalance.text = "${state.balance} $"
  }
}