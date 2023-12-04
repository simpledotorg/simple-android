package org.simple.clinic.onboarding

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class OnboardingEffectHandler @AssistedInject constructor(
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
        .addConsumer(OnboardingViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }
}
