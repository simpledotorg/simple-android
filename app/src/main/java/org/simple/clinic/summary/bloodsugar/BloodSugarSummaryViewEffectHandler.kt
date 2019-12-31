package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

object BloodSugarSummaryViewEffectHandler {

  fun create(
      bloodSugarRepository: BloodSugarRepository,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<BloodSugarSummaryViewEffect, BloodSugarSummaryViewEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarSummaryViewEffect, BloodSugarSummaryViewEvent>()
        .addTransformer(FetchBloodSugarSummary::class.java) { effect ->
          effect
              .flatMap {
                bloodSugarRepository.latestMeasurements(it.patientUuid, 100)
                    .subscribeOn(schedulersProvider.io())
              }
              .map { BloodSugarSummaryFetched(it) }
        }
        .build()
  }
}
