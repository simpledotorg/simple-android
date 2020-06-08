package org.simple.clinic.registration.name

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class RegistrationNameEffectHandler @Inject constructor() {

  fun build(): ObservableTransformer<RegistrationNameEffect, RegistrationNameEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationNameEffect, RegistrationNameEvent>()
        .build()
  }
}
