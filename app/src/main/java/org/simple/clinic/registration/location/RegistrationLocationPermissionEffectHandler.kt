package org.simple.clinic.registration.location

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationLocationPermissionEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: RegistrationLocationPermissionUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: RegistrationLocationPermissionUiActions): RegistrationLocationPermissionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationLocationPermissionEffect, RegistrationLocationPermissionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationLocationPermissionEffect, RegistrationLocationPermissionEvent>()
        .addConsumer(OpenFacilitySelectionScreen::class.java, { uiActions.openFacilitySelectionScreen(it.entry) }, schedulers.ui())
        .build()
  }
}
