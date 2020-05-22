package org.simple.clinic.home.patients

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class PatientsEffectHandler @Inject constructor() {

  fun build(): ObservableTransformer<PatientsEffect, PatientsEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientsEffect, PatientsEvent>()
        .build()
  }
}
