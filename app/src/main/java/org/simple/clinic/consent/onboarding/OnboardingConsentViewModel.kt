package org.simple.clinic.consent.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spotify.mobius.rx2.RxMobius
import org.simple.clinic.mobius.MobiusViewModel

class OnboardingConsentViewModel(
    savedStateHandle: SavedStateHandle,
    effectHandlerFactory: OnboardingConsentEffectHandler.Factory
) : MobiusViewModel<OnboardingConsentModel, OnboardingConsentEvent, OnboardingConsentEffect, OnboardingConsentViewEffect>(
    modelKey = OnboardingConsentModel::class.java.name,
    savedStateHandle = savedStateHandle,
    defaultModel = OnboardingConsentModel,
    loopFactoryProvider = { viewEffectsConsumer ->
      RxMobius.loop(
          OnboardingConsentUpdate(),
          effectHandlerFactory.create(viewEffectsConsumer).build()
      )
    }
) {

  companion object {
    fun factory(effectHandlerFactory: OnboardingConsentEffectHandler.Factory) = viewModelFactory {
      initializer {
        OnboardingConsentViewModel(
            savedStateHandle = createSavedStateHandle(),
            effectHandlerFactory = effectHandlerFactory
        )
      }
    }
  }
}
