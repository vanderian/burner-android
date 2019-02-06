package com.vander.burner.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import at.favre.lib.hood.Hood
import at.favre.lib.hood.interfaces.actions.ManagerControl
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.vander.burner.BuildConfig
import com.vander.burner.app.di.Permanent
import com.vander.burner.app.di.XDaiProvider
import com.vander.burner.app.ui.DebugActivity
import com.vander.scaffold.annotations.ActivityScope
import com.vander.scaffold.annotations.ApplicationScope
import com.vander.scaffold.debug.riseAndShine
import com.vander.scaffold.debugyzer.bugreport.BugReportContainer
import com.vander.scaffold.debugyzer.bugreport.ReportData
import com.vander.scaffold.ui.ActivityHierarchyServer
import com.vander.scaffold.ui.ViewContainer
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@Module(includes = [App.Module::class])
object DebugAppModule {

  private const val SUPPORT_CONTACT_EMAIL = "vanderka.marian+burner@gmail.com"

  @Module
  abstract class Activities {
    @ActivityScope @ContributesAndroidInjector()
    abstract fun contributeDebugActivity(): DebugActivity

  }

  @JvmStatic @Provides @ApplicationScope fun providesViewContainer(context: Context): ViewContainer =
      BugReportContainer(context, ReportData(SUPPORT_CONTACT_EMAIL, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE), { true })

  @JvmStatic @Provides @ApplicationScope @IntoSet
  fun providesHierarchyServer(): ActivityHierarchyServer = object : ActivityHierarchyServer.Debug() {
    lateinit var shake: ManagerControl

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
      super.onActivityCreated(p0, p1)
      riseAndShine(p0)

//    debug blockcanary
      val permissions = arrayOf(
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_PHONE_STATE
      )
      if (permissions.map { ActivityCompat.checkSelfPermission(p0, it) }.any { it == PackageManager.PERMISSION_DENIED }) {
        ActivityCompat.requestPermissions(p0, permissions, 0)
      }
      shake = Hood.ext().registerShakeToOpenDebugActivity(p0, Intent(p0, DebugActivity::class.java))
    }

    override fun onActivityResumed(p0: Activity) {
      super.onActivityResumed(p0)
      shake.start()
    }

    override fun onActivityPaused(p0: Activity) {
      super.onActivityPaused(p0)
      shake.stop()
    }
  }

  @JvmStatic @Provides @ApplicationScope
  fun providesOkHttpBuilder(okHttp: OkHttpClient.Builder): OkHttpClient = okHttp
      .addNetworkInterceptor(StethoInterceptor())
      .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
      .build()

  @JvmStatic @Provides @Reusable @XDaiProvider
  fun providesXDaiProviderPref(@Permanent prefs: RxSharedPreferences): Preference<String> =
      prefs.getString("pref_url_xdai", "http://localhost:8545")

  @JvmStatic @Provides @Reusable @XDaiProvider
  fun providesXDaiProviderUrl(@XDaiProvider pref: Preference<String>): HttpUrl = HttpUrl.get(pref.get())!!

}