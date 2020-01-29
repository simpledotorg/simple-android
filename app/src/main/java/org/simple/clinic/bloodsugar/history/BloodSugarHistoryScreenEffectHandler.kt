package org.simple.clinic.bloodsugar.history

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class BloodSugarHistoryScreenEffectHandler @Inject constructor(
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<BloodSugarHistoryScreenEffect, BloodSugarHistoryScreenEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarHistoryScreenEffect, BloodSugarHistoryScreenEvent>()
        .addTransformer(LoadPatient::class.java, loadPatient(schedulersProvider.io()))
        .build()
  }

  private fun loadPatient(
      scheduler: Scheduler
  ): ObservableTransformer<LoadPatient, BloodSugarHistoryScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            patientRepository
                .patient(it.patientUuid)
                .take(1)
                .subscribeOn(scheduler)
          }
          .filterAndUnwrapJust()
          .map(::PatientLoaded)
    }
  }
}
