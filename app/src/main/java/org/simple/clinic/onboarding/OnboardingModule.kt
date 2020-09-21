package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.OnboardingComplete

@Module
class OnboardingModule {

  @Provides
  @TypedPreference(OnboardingComplete)
  fun hasTheUserCompletedOnboarding(rxSharedPreferences: RxSharedPreferences): Preference<Boolean> = rxSharedPreferences.getBoolean("onboarding_complete")
}
