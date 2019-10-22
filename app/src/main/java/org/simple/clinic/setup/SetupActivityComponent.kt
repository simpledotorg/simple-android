package org.simple.clinic.setup

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.activity.BindsScreenRouter
import org.simple.clinic.onboarding.OnboardingScreenInjector

@Subcomponent
interface SetupActivityComponent: OnboardingScreenInjector {

  fun inject(target: SetupActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder>, BindsScreenRouter<Builder> {

    fun build(): SetupActivityComponent
  }
}
