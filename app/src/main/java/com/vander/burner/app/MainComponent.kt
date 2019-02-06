package com.vander.burner.app

import autodagger.AutoComponent
import com.vander.burner.app.data.DataModule
import com.vander.burner.app.net.NetModule
import com.vander.burner.app.ui.screen.UiModules

@AutoComponent(
    modules = [
      InjectorFactoriesModules.Activities::class,
      UiModules.ViewModelModules::class,
      DataModule::class,
      NetModule::class
    ]
)
annotation class MainComponent