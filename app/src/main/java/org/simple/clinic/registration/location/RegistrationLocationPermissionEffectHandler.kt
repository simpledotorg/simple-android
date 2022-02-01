package org.simple.clinic.registration.location

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationLocationPermissionEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: RegistrationLocationPermissionUiActions,
    @Assisted private val viewEffectConsumer: Consumer<RegistrationLocationPermissionViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: RegistrationLocationPermissionUiActions,
        viewEffectConsumer: Consumer<RegistrationLocationPermissionViewEffect>
    ): RegistrationLocationPermissionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationLocationPermissionEffect, RegistrationLocationPermissionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationLocationPermissionEffect, RegistrationLocationPermissionEvent>()
        .addConsumer(OpenFacilitySelectionScreen::class.java, { uiActions.openFacilitySelectionScreen(it.entry) }, schedulers.ui())
        .addConsumer(RegistrationLocationPermissionViewEffect::class.java, viewEffectConsumer::accept)
        .build()
  }
}
