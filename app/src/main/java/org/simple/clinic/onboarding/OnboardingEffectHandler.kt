package org.simple.clinic.onboarding

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.OnboardingComplete

class OnboardingEffectHandler @AssistedInject constructor(
    @TypedPreference(OnboardingComplete) private val hasUserCompletedOnboarding: Preference<Boolean>,
    @Assisted private val viewEffectsConsumer: Consumer<OnboardingViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<OnboardingViewEffect>
    ): OnboardingEffectHandler
  }

  fun build(): ObservableTransformer<OnboardingEffect, OnboardingEvent> {
    return RxMobius
        .subtypeEffectHandler<OnboardingEffect, OnboardingEvent>()
        .addTransformer(CompleteOnboardingEffect::class.java, completeOnboardingTransformer())
        .addConsumer(OnboardingViewEffect::class.java, viewEffectsConsumer::accept)
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
