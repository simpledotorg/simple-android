package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.OnboardingComplete
import org.simple.clinic.util.scheduler.SchedulersProvider

class OnboardingEffectHandler @AssistedInject constructor(
    @TypedPreference(OnboardingComplete) private val hasUserCompletedOnboarding: Preference<Boolean>,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val ui: OnboardingUi
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(ui: OnboardingUi): OnboardingEffectHandler
  }

  fun build(): ObservableTransformer<OnboardingEffect, OnboardingEvent> {
    return RxMobius
        .subtypeEffectHandler<OnboardingEffect, OnboardingEvent>()
        .addTransformer(CompleteOnboardingEffect::class.java, completeOnboardingTransformer())
        .addAction(MoveToRegistrationEffect::class.java, ui::moveToRegistrationScreen, schedulersProvider.ui())
        .build()
  }

  private fun completeOnboardingTransformer(): ObservableTransformer<CompleteOnboardingEffect, OnboardingEvent> {
    return ObservableTransformer { completeOnboardingEffect ->
      completeOnboardingEffect
          .doOnNext { hasUserCompletedOnboarding.set(true) }
          .map { OnboardingCompleted }
    }
  }
}

