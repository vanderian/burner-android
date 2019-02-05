package com.vander.burner.app

import android.os.StrictMode
import autodagger.AutoInjector
import com.facebook.stetho.Stetho
import com.github.moduth.blockcanary.BlockCanary
import com.github.moduth.blockcanary.BlockCanaryContext
import com.squareup.leakcanary.LeakCanary
import timber.log.Timber

@AutoInjector(App::class)
class DebugApp : App() {
  private fun setupAppMonitoring() {
    LeakCanary.install(this)
    BlockCanary.install(this, BlockCanaryContext()).start()
  }

  override fun onCreate() {
    System.setProperty("rx2.buffer-size", "10")

    Timber.plant(Timber.DebugTree())
    if (LeakCanary.isInAnalyzerProcess(this)) {
      return
    }
    super.onCreate()
    Stetho.initializeWithDefaults(this)

    StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().penaltyLog().penaltyFlashScreen().detectAll().build())
    StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().penaltyLog().detectAll().build())
  }
}
