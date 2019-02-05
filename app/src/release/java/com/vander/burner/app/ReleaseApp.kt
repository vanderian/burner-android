package com.vander.burner.app

import autodagger.AutoInjector

@AutoInjector(App::class)
class ReleaseApp : App() {
  override fun onCreate() {
    super.onCreate()
  }
}
