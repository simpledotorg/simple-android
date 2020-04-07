package org.simple.clinic.patientcontact

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class PatientContactEffectHandler {

  companion object {
    fun create(): ObservableTransformer<PatientContactEffect, PatientContactEvent> {
      return PatientContactEffectHandler()
          .buildEffectHandler()
    }
  }

  private fun buildEffectHandler(): ObservableTransformer<PatientContactEffect, PatientContactEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientContactEffect, PatientContactEvent>()
        .build()
  }
}
