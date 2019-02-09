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

    @ScreenScope @ContributesAndroidInjector()
    abstract fun contributeInitScreen(): InitScreen

    @ScreenScope @ContributesAndroidInjector()
    abstract fun contributeReceiveScreen(): ReceiveScreen

  }

  @Module
  abstract class ViewModelModules {
    @Binds @IntoMap @ViewModelKey(BalanceModel::class)
    abstract fun provideBalanceModel(viewModel: BalanceModel): ViewModel

    @Binds @IntoMap @ViewModelKey(InitModel::class)
    abstract fun provideInitModel(viewModel: InitModel): ViewModel

    @Binds @IntoMap @ViewModelKey(ReceiveModel::class)
    abstract fun provideReceiveModel(viewModel: ReceiveModel): ViewModel

  }
}