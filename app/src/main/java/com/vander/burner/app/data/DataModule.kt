package com.vander.burner.app.data

import android.content.Context
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.vander.burner.BuildConfig
import com.vander.burner.app.di.Permanent
import com.vander.scaffold.annotations.ApplicationScope
import dagger.Module
import dagger.Provides
import pm.gnosis.mnemonic.Bip39
import pm.gnosis.mnemonic.Bip39Generator
import pm.gnosis.mnemonic.android.AndroidWordListProvider
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString

@Module
object DataModule {

  @JvmStatic @ApplicationScope @Provides
  fun providesBip39(ctx: Context): Bip39 = Bip39Generator(AndroidWordListProvider(ctx))

  @JvmStatic @ApplicationScope @Provides
  fun providesPairedAddress(@Permanent prefs: RxSharedPreferences): Preference<Solidity.Address> =
      prefs.getObject("user.account.paired", BuildConfig.UNSPENT_SINK.asEthereumAddress()!!, object : Preference.Converter<Solidity.Address> {
        override fun deserialize(serialized: String): Solidity.Address = serialized.asEthereumAddress()!!

        override fun serialize(value: Solidity.Address): String = value.asEthereumAddressString()
      })
}