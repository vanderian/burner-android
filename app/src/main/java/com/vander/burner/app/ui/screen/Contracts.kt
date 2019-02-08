package com.vander.burner.app.ui.screen

import com.vander.scaffold.screen.Screen
import io.reactivex.Observable
import pm.gnosis.model.Solidity
import java.math.BigDecimal

data class BalanceState(
    val address: Solidity.Address,
    val balance: BigDecimal = BigDecimal.ZERO
) : Screen.State

interface BalanceIntents : Screen.Intents {
  fun burn(): Observable<Unit>
}