package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class NewMedicalHistoryEffectHandler {

  fun build(): ObservableTransformer<NewMedicalHistoryEffect, NewMedicalHistoryEvent> {
    return RxMobius
        .subtypeEffectHandler<NewMedicalHistoryEffect, NewMedicalHistoryEvent>()
        .build()
  }
}
