package org.simple.clinic.setup

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.onboarding.OnboardingScreenInjector
import org.simple.clinic.splash.SplashScreen

@Subcomponent(modules = [SetupActivityModule::class])
interface SetupActivityComponent : OnboardingScreenInjector, SplashScreen.Injector {

  fun inject(target: SetupActivity)

  @Subcomponent.Factory
  interface Factory {
    fun create(
        @BindsInstance activity: AppCompatActivity,
        @BindsInstance router: Router
    ): SetupActivityComponent
  }
}
