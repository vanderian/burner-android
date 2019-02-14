package com.vander.burner.app.ui

import android.os.Bundle
import androidx.navigation.NavGraph
import com.google.firebase.analytics.FirebaseAnalytics
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    navController.addOnDestinationChangedListener { _, destination, _ ->
      FirebaseAnalytics.getInstance(this).setCurrentScreen(this, destination.label.toString(), null)
    }
  }

}