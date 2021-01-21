package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class ConfirmRemoveBloodSugarEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val bloodSugarRepository: BloodSugarRepository,
    @Assisted private val uiActions: ConfirmRemoveBloodSugarUiActions,
    private val schedulersProvider: SchedulersProvider
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ConfirmRemoveBloodSugarUiActions): ConfirmRemoveBloodSugarEffectHandler
  }

  fun build(): ObservableTransformer<ConfirmRemoveBloodSugarEffect, ConfirmRemoveBloodSugarEvent> {
    return RxMobius
        .subtypeEffectHandler<ConfirmRemoveBloodSugarEffect, ConfirmRemoveBloodSugarEvent>()
        .addTransformer(MarkBloodSugarAsDeleted::class.java, markBloodSugarAsDeleted(schedulersProvider.io()))
        .addAction(CloseConfirmRemoveBloodSugarDialog::class.java, { uiActions.closeDialog() }, schedulersProvider.ui())
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
