package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class ConfirmRemoveBloodSugarEffectHandler @Inject constructor(
    private val patientRepository: PatientRepository,
    private val bloodSugarRepository: BloodSugarRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<ConfirmRemoveBloodSugarEffect, ConfirmRemoveBloodSugarEvent> {
    return RxMobius
        .subtypeEffectHandler<ConfirmRemoveBloodSugarEffect, ConfirmRemoveBloodSugarEvent>()
        .addTransformer(MarkBloodSugarAsDeleted::class.java, markBloodSugarAsDeleted(schedulersProvider.io()))
        .addAction(CloseConfirmRemoveBloodSugarDialog::class.java, {
          // TODO (SM): Close confirm remove blood sugar dialog
        })
        .build()
  }

  private fun markBloodSugarAsDeleted(
      scheduler: Scheduler
  ): ObservableTransformer<MarkBloodSugarAsDeleted, ConfirmRemoveBloodSugarEvent> {
    return ObservableTransformer { markBloodSugarAsDeletedStream ->
      markBloodSugarAsDeletedStream
          .observeOn(scheduler)
          .map { bloodSugarRepository.measurement(it.bloodSugarMeasurementUuid)!! }
          .doOnNext(bloodSugarRepository::markBloodSugarAsDeleted)
          .flatMap {
            patientRepository.updateRecordedAt(it.patientUuid)
                .andThen(Observable.just(BloodSugarMarkedAsDeleted))
          }
    }
  }
}
