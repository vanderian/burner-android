package com.vander.burner.app.ui

import androidx.navigation.NavGraph
import com.vander.burner.R
import com.vander.burner.app.data.AccountRepository
import com.vander.scaffold.ui.NavigationActivity
import javax.inject.Inject

class MainActivity : NavigationActivity() {
  @Inject lateinit var accountStorage: AccountRepository

  override val graphId: Int = R.navigation.main

  override fun graph(): NavGraph {
    return super.graph().apply {
      if (accountStorage.hasCredentials) {
        startDestination = R.id.balanceScreen
      }
    }
  }

}