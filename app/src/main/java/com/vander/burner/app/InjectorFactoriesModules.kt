package com.vander.burner.app

import com.vander.burner.app.ui.MainActivity
import com.vander.scaffold.annotations.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * @author marian on 21.9.2017.
 */
object InjectorFactoriesModules {

  @Module
  abstract class Activities {
    @ActivityScope @ContributesAndroidInjector(modules = [ActivityModules::class])
    abstract fun contributeMainActivity(): MainActivity
  }

  @Module(includes = [
  ])
  abstract class ActivityModules
}