package org.simple.clinic.registration.facility

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationFacilitySelectionEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: RegistrationFacilitySelectionUiActions,
    @Assisted private val viewEffectsConsumer: Consumer<RegistrationFacilitySelectionViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: RegistrationFacilitySelectionUiActions,
        viewEffectsConsumer: Consumer<RegistrationFacilitySelectionViewEffect>
    ): RegistrationFacilitySelectionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent>()
        .addConsumer(MoveToIntroVideoScreen::class.java, { uiActions.openIntroVideoScreen(it.registrationEntry) }, schedulersProvider.ui())
        .addConsumer(RegistrationFacilitySelectionViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }
}
