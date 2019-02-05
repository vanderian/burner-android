package com.vander.burner.app.net

import com.vander.scaffold.annotations.ApplicationScope
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient

@Module
object NetModule {

  @JvmStatic @Provides @ApplicationScope
  fun providesOkHttpBuilder(): OkHttpClient.Builder =
      OkHttpClient.Builder()

}
