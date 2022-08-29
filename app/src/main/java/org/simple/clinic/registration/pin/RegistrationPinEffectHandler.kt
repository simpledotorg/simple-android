package org.simple.clinic.registration.pin

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class RegistrationPinEffectHandler @AssistedInject constructor(
    @Assisted private val viewEffectsConsumer: Consumer<RegistrationPinViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<RegistrationPinViewEffect>
    ): RegistrationPinEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationPinEffect, RegistrationPinEvent> {
    return RxMobius.subtypeEffectHandler<RegistrationPinEffect, RegistrationPinEvent>()
        .addConsumer(RegistrationPinViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }
}
