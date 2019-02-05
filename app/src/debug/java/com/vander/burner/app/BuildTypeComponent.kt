package com.vander.burner.app

import autodagger.AutoComponent
import autodagger.AutoInjector
import com.vander.scaffold.annotations.ApplicationScope

@AutoComponent(
    modules = [DebugAppModule::class, DebugAppModule.Activities::class],
    includes = MainComponent::class
)
@AutoInjector
@ApplicationScope
annotation class BuildTypeComponent