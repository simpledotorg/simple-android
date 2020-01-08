package org.simple.clinic.summary

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class PatientSummaryEffectHandler {

  fun build(): ObservableTransformer<PatientSummaryEffect, PatientSummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSummaryEffect, PatientSummaryEvent>()
        .build()
  }
}
