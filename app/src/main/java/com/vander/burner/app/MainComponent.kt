package com.vander.burner.app

import autodagger.AutoComponent
import com.vander.burner.app.data.DataModule
import com.vander.burner.app.net.NetModule

@AutoComponent(
    modules = [
      InjectorFactoriesModules.Activities::class,
      DataModule::class,
      NetModule::class
    ]
)
annotation class MainComponent