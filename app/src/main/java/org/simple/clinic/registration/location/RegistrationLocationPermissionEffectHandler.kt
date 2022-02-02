package org.simple.clinic.registration.location

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class RegistrationLocationPermissionEffectHandler @AssistedInject constructor(
    @Assisted private val viewEffectConsumer: Consumer<RegistrationLocationPermissionViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectConsumer: Consumer<RegistrationLocationPermissionViewEffect>
    ): RegistrationLocationPermissionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationLocationPermissionEffect, RegistrationLocationPermissionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationLocationPermissionEffect, RegistrationLocationPermissionEvent>()
        .addConsumer(RegistrationLocationPermissionViewEffect::class.java, viewEffectConsumer::accept)
        .build()
  }
}
