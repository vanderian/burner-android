package com.vander.burner.app

import autodagger.AutoComponent
import autodagger.AutoInjector
import com.vander.scaffold.annotations.ApplicationScope

@AutoComponent(
    modules = [ReleaseAppModule::class],
    includes = MainComponent::class
)
@AutoInjector
@ApplicationScope
annotation class BuildTypeComponent