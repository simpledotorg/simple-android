package org.simple.clinic.home.report

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class ReportsEffectHandler @Inject constructor() {
  fun build(): ObservableTransformer<ReportsEffect, ReportsEvent> = RxMobius
      .subtypeEffectHandler<ReportsEffect, ReportsEvent>()
      .build()
}
