package com.vander.burner.app.data

import android.content.Context
import com.vander.scaffold.annotations.ApplicationScope
import dagger.Module
import dagger.Provides
import pm.gnosis.mnemonic.Bip39
import pm.gnosis.mnemonic.Bip39Generator
import pm.gnosis.mnemonic.android.AndroidWordListProvider

@Module
object DataModule {

  @JvmStatic @ApplicationScope @Provides
  fun provideBip39(ctx: Context): Bip39 = Bip39Generator(AndroidWordListProvider(ctx))
}