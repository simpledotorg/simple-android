package org.simple.clinic.registration.pin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class RegistrationPinEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: RegistrationPinUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationPinUiActions): RegistrationPinEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationPinEffect, RegistrationPinEvent> {
    return RxMobius.subtypeEffectHandler<RegistrationPinEffect, RegistrationPinEvent>()
        .build()
  }
}
