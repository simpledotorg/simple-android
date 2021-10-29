package org.simple.clinic.setup

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.onboarding.OnboardingScreen
import org.simple.clinic.splash.SplashScreen

@Subcomponent(modules = [SetupActivityModule::class])
interface SetupActivityComponent : OnboardingScreen.Injector, SplashScreen.Injector {

  fun inject(target: SetupActivity)

  @Subcomponent.Factory
  interface Factory {
    fun create(
        @BindsInstance activity: AppCompatActivity,
        @BindsInstance router: Router
    ): SetupActivityComponent
  }
}
