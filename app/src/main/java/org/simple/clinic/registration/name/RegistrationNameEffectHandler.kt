package org.simple.clinic.registration.name

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer

class RegistrationNameEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: RegistrationNameUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationNameUiActions): RegistrationNameEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationNameEffect, RegistrationNameEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationNameEffect, RegistrationNameEvent>()
        .build()
  }
}
