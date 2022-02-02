package org.simple.clinic.registration.facility

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class RegistrationFacilitySelectionEffectHandler @AssistedInject constructor(
    @Assisted private val viewEffectsConsumer: Consumer<RegistrationFacilitySelectionViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<RegistrationFacilitySelectionViewEffect>
    ): RegistrationFacilitySelectionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent>()
        .addConsumer(RegistrationFacilitySelectionViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }
}
