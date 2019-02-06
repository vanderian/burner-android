package com.vander.burner.app.ui.screen

import androidx.lifecycle.ViewModel
import com.vander.scaffold.annotations.ScreenScope
import com.vander.scaffold.annotations.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

object UiModules {

  @Module
  abstract class ScreenModules {
    @ScreenScope @ContributesAndroidInjector()
    abstract fun contributeBalanceScreen(): BalanceScreen

  }

  @Module
  abstract class ViewModelModules {
    @Binds @IntoMap @ViewModelKey(BalanceModel::class)
    abstract fun provideBalanceModel(viewModel: BalanceModel): ViewModel

  }
}