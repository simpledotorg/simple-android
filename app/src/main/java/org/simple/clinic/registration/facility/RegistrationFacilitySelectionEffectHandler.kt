package org.simple.clinic.registration.facility

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationFacilitySelectionEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: RegistrationFacilitySelectionUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationFacilitySelectionUiActions): RegistrationFacilitySelectionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent>()
        .build()
  }
}
