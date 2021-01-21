package org.simple.clinic.registration.facility

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationFacilitySelectionEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: RegistrationFacilitySelectionUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: RegistrationFacilitySelectionUiActions): RegistrationFacilitySelectionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent>()
        .addConsumer(OpenConfirmFacilitySheet::class.java, { uiActions.showConfirmFacilitySheet(it.facility.uuid, it.facility.name) }, schedulersProvider.ui())
        .addConsumer(MoveToIntroVideoScreen::class.java, { uiActions.openIntroVideoScreen(it.registrationEntry) }, schedulersProvider.ui())
        .build()
  }
}
