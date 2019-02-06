package com.vander.burner.app.net

import com.vander.burner.app.di.XDaiProvider
import com.vander.scaffold.annotations.ApplicationScope
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.JsonRpc2_0Web3j
import org.web3j.protocol.http.HttpService

@Module
object NetModule {

  @JvmStatic @Provides @ApplicationScope
  fun providesOkHttpBuilder(): OkHttpClient.Builder =
      OkHttpClient.Builder()

  @JvmStatic @Provides @ApplicationScope @XDaiProvider
  fun providesWeb3j(client: OkHttpClient, @XDaiProvider url: HttpUrl): Web3j = JsonRpc2_0Web3j(HttpService(url.toString(), client))

}
