package org.simple.clinic.registration.location

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class RegistrationLocationPermissionEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): RegistrationLocationPermissionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationLocationPermissionEffect, RegistrationLocationPermissionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationLocationPermissionEffect, RegistrationLocationPermissionEvent>()
        .build()
  }
}
