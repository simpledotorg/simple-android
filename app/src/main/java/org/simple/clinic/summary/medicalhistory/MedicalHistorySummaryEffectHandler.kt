package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class MedicalHistorySummaryEffectHandler {

  fun create(): ObservableTransformer<MedicalHistorySummaryEffect, MedicalHistorySummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<MedicalHistorySummaryEffect, MedicalHistorySummaryEvent>()
        .build()
  }
}
