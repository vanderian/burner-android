package com.vander.burner.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.crashlytics.android.Crashlytics
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.google.firebase.FirebaseApp
import com.vander.burner.app.di.Permanent
import com.vander.burner.app.di.ScreenSize
import com.vander.scaffold.BaseApp
import com.vander.scaffold.BaseAppModule
import dagger.Provides
import de.adorsys.android.securestoragelibrary.SecurePreferences
import io.fabric.sdk.android.Fabric
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import me.dm7.barcodescanner.core.DisplayUtils
import pm.gnosis.crypto.LinuxSecureRandom
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.min

@BuildTypeComponent
abstract class App : BaseApp() {
  @Inject @field:Permanent protected lateinit var prefs: RxSharedPreferences

  override fun buildComponentAndInject() = Initializer.init(this).inject(this)

  override fun onCreate() {
    super.onCreate()
    setupRx()
    clearKeyStoreOnInstall()

    Fabric.with(this, Crashlytics())

    try {
      LinuxSecureRandom()
    } catch (e: Exception) {
      Timber.e("Could not register LinuxSecureRandom. Using default SecureRandom.")
    }
  }

  private fun clearKeyStoreOnInstall() {
    with(prefs.getBoolean("app.first_run")) {
      if (!isSet) {
        set(true)
        SecurePreferences.clearAllValues()
      }
    }
  }

  @dagger.Module(includes = [BaseAppModule::class])
  object Module {

    @JvmStatic @Provides fun providesPackageManager(app: Application): PackageManager = app.packageManager

    @JvmStatic @Provides fun providesPreferences(app: Application): SharedPreferences =
        app.getSharedPreferences("wallet_preferences", Context.MODE_PRIVATE)

    @JvmStatic @Provides fun providesRxPreferences(prefs: SharedPreferences): RxSharedPreferences = RxSharedPreferences.create(prefs)

    @JvmStatic @Provides @Permanent fun providesPermanentPreferences(app: Application): SharedPreferences =
        app.getSharedPreferences("permanent_wallet_preferences", Context.MODE_PRIVATE)

    @JvmStatic @Provides @Permanent fun providesPermanentRxPreferences(@Permanent prefs: SharedPreferences): RxSharedPreferences =
        RxSharedPreferences.create(prefs)

    @JvmStatic @Provides fun providesUuidPreference(@Permanent prefs: RxSharedPreferences): UUID {
      val pref = prefs.getString("app.uuid")
      if (!pref.isSet) pref.set(UUID.randomUUID().toString())
      return UUID.fromString(pref.get())
    }

    @JvmStatic @Provides @ScreenSize fun providesSmallestWidth(context: Context): Int =
        DisplayUtils.getScreenResolution(context).run { min(x, y) }
  }
}

