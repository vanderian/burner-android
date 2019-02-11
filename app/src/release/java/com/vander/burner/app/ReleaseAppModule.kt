package com.vander.burner.app

import com.vander.burner.app.di.Xdai
import com.vander.scaffold.ui.ViewContainer
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

@Module(includes = [(App.Module::class)])
object ReleaseAppModule {

  @JvmStatic @Provides fun providesViewContainer() = ViewContainer.DEFAULT

  @JvmStatic @Provides fun providesOkHttpBuilder(okHttp: OkHttpClient.Builder): OkHttpClient = okHttp.build()

  @JvmStatic @Provides @Reusable @Xdai
  fun providesXDaiProviderUrl(): HttpUrl = HttpUrl.parse("https://dai.poa.network")!!

}