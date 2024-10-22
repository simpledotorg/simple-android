package org.simple.clinic.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.MobiusLoopViewModel
import com.spotify.mobius.android.runners.MainThreadWorkRunner
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.functions.Function
import com.spotify.mobius.rx2.RxMobius
import org.simple.clinic.mobius.MobiusViewModel
import org.simple.clinic.mobius.first
import org.simple.clinic.platform.analytics.Analytics

class OnboardingViewModel(
  savedStateHandle: SavedStateHandle,
  effectHandlerFactory: OnboardingEffectHandler.Factory
) : MobiusViewModel<OnboardingModel, OnboardingEvent, OnboardingEffect, OnboardingViewEffect>(
  modelKey = OnboardingModel::class.java.name,
  savedStateHandle = savedStateHandle,
  defaultModel = OnboardingModel,
  loopFactoryProvider = { viewEffectsConsumer ->
    RxMobius.loop(
      OnboardingUpdate(),
      effectHandlerFactory.create(viewEffectsConsumer).build()
    )
  }
) {

  companion object {
    fun factory(effectHandlerFactory: OnboardingEffectHandler.Factory) = viewModelFactory {
      initializer {
        OnboardingViewModel(
          savedStateHandle = createSavedStateHandle(),
          effectHandlerFactory = effectHandlerFactory
        )
      }
    }
  }
}
