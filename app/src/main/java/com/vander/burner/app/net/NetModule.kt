package com.vander.burner.app.net

import com.squareup.moshi.Moshi
import com.vander.burner.app.di.Xdai
import com.vander.burner.app.net.adapters.*
import com.vander.scaffold.annotations.ApplicationScope
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import pm.gnosis.ethereum.EthereumRepository
import pm.gnosis.ethereum.rpc.RpcEthereumRepository
import pm.gnosis.ethereum.rpc.retrofit.RetrofitEthereumRpcApi
import pm.gnosis.ethereum.rpc.retrofit.RetrofitEthereumRpcConnector
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
object NetModule {
  const val RETRY_COUNT = 2

  @JvmStatic @Provides @ApplicationScope
  fun providesMoshi(): Moshi {
    return Moshi.Builder()
        .add(WeiAdapter())
        .add(HexNumberAdapter())
        .add(DecimalNumberAdapter())
        .add(DefaultNumberAdapter())
        .add(SolidityAddressAdapter())
        .build()
  }

  @JvmStatic @Provides @ApplicationScope
  fun providesOkHttpBuilder(): OkHttpClient.Builder =
      OkHttpClient.Builder()

  @JvmStatic @Provides @ApplicationScope
  fun providesEthereumJsonRpcApi(moshi: Moshi, client: OkHttpClient, @Xdai url: HttpUrl): RetrofitEthereumRpcApi =
      Retrofit.Builder()
          .client(client)
          .baseUrl(url)
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
          .build()
          .create(RetrofitEthereumRpcApi::class.java)

  @JvmStatic @Provides @ApplicationScope @Xdai
  fun providesXdaiRepository(api: RetrofitEthereumRpcApi): EthereumRepository = RpcEthereumRepository(RetrofitEthereumRpcConnector(api))

}
