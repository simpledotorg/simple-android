package org.simple.clinic.registration.confirmpin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class RegistrationConfirmPinEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: RegistrationConfirmPinUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationConfirmPinUiActions): RegistrationConfirmPinEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationConfirmPinEffect, RegistrationConfirmPinEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationConfirmPinEffect, RegistrationConfirmPinEvent>()
        .build()
  }
}
