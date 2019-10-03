package org.simple.clinic.newentry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

object PatientEntryEffectHandler {
  fun createEffectHandler(
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<PatientEntryEffect, PatientEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientEntryEffect, PatientEntryEvent>()
        .build()
  }
}
