package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class BloodSugarSummaryViewEffectHandler {

  companion object {
    fun create(): ObservableTransformer<BloodSugarSummaryViewEffect, BloodSugarSummaryViewEvent> {
      return RxMobius
          .subtypeEffectHandler<BloodSugarSummaryViewEffect, BloodSugarSummaryViewEvent>()
          .addTransformer(FetchBloodSugarSummary::class.java) { effect -> effect.map { BloodSugarSummaryFetched } }
          .build()
    }
  }
}
