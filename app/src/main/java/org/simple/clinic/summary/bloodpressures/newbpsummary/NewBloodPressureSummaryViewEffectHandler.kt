package org.simple.clinic.summary.bloodpressures.newbpsummary

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class NewBloodPressureSummaryViewEffectHandler @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<NewBloodPressureSummaryViewEffect, NewBloodPressureSummaryViewEvent> {
    return RxMobius
        .subtypeEffectHandler<NewBloodPressureSummaryViewEffect, NewBloodPressureSummaryViewEvent>()
        .addTransformer(LoadBloodPressures::class.java, loadBloodPressureHistory(schedulersProvider.io()))
        .build()
  }

  private fun loadBloodPressureHistory(
      scheduler: Scheduler
  ): ObservableTransformer<LoadBloodPressures, NewBloodPressureSummaryViewEvent> {
    return ObservableTransformer { effect ->
      effect
          .switchMap {
            bloodPressureRepository
                .newestMeasurementsForPatient(it.patientUuid, it.numberOfBpsToDisplay)
                .subscribeOn(scheduler)
          }
          .map(::BloodPressuresLoaded)
    }
  }
}
