package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class OnboardingModule {

  @Provides
  @Named("onboarding_complete")
  fun hasTheUserCompletedOnboarding(rxSharedPreferences: RxSharedPreferences): Preference<Boolean> = rxSharedPreferences.getBoolean("onboarding_complete")
}