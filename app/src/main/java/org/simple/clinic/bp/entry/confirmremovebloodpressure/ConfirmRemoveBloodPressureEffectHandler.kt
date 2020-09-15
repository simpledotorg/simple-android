package org.simple.clinic.bp.entry.confirmremovebloodpressure

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class ConfirmRemoveBloodPressureEffectHandler @AssistedInject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: ConfirmRemoveBloodPressureDialogUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: ConfirmRemoveBloodPressureDialogUiActions): ConfirmRemoveBloodPressureEffectHandler
  }

  fun build(): ObservableTransformer<ConfirmRemoveBloodPressureEffect,
      ConfirmRemoveBloodPressureEvent> = RxMobius
      .subtypeEffectHandler<ConfirmRemoveBloodPressureEffect, ConfirmRemoveBloodPressureEvent>()
      .addAction(CloseDialog::class.java, uiActions::closeDialog, schedulersProvider.ui())
      .addTransformer(DeleteBloodPressure::class.java, deleteBloodPressure())
      .build()

  private fun deleteBloodPressure(): ObservableTransformer<DeleteBloodPressure, ConfirmRemoveBloodPressureEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { bloodPressureRepository.measurement(it.bloodPressureMeasurementUuid) }
          .switchMap { bloodPressureMeasurement ->
            bloodPressureRepository.markBloodPressureAsDeleted(bloodPressureMeasurement)
                .andThen(patientRepository.updateRecordedAt(bloodPressureMeasurement.patientUuid))
                .andThen(Observable.just(BloodPressureDeleted))
          }
    }
  }
}
