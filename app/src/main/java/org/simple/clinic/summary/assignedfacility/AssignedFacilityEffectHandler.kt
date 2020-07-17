package org.simple.clinic.summary.assignedfacility

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class AssignedFacilityEffectHandler {
  fun build(): ObservableTransformer<AssignedFacilityEffect, AssignedFacilityEvent> = RxMobius
      .subtypeEffectHandler<AssignedFacilityEffect, AssignedFacilityEvent>()
      .build()
}
