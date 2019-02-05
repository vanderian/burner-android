package com.vander.burner.app

import com.vander.scaffold.BaseAppModule

object Initializer {
  fun init(app: App): AppComponent =
      DaggerAppComponent.builder()
          .baseAppModule(BaseAppModule(app))
          .build()
}