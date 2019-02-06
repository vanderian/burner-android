package com.vander.burner.app.ui.screen

import com.vander.scaffold.screen.Screen
import java.math.BigDecimal

data class BalanceState(
    val balance: BigDecimal = BigDecimal.ZERO
) : Screen.State

interface BalanceIntents : Screen.Intents