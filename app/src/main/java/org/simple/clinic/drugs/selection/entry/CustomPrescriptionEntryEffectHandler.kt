package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class CustomPrescriptionEntryEffectHandler {

  companion object {
    fun create(): ObservableTransformer<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent> {
      return CustomPrescriptionEntryEffectHandler().buildEffectHandler()
    }
  }

  private fun buildEffectHandler()
      : ObservableTransformer<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent>()
        .build()
  }
}
