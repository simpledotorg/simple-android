package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class DosagePickerEffectHandler {

  fun build(): ObservableTransformer<DosagePickerEffect, DosagePickerEvent> {
    return RxMobius
        .subtypeEffectHandler<DosagePickerEffect, DosagePickerEvent>()
        .build()
  }
}
