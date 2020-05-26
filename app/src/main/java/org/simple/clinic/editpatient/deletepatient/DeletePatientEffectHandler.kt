package org.simple.clinic.editpatient.deletepatient

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class DeletePatientEffectHandler @AssistedInject constructor(
    val patientRepository: PatientRepository,
    val schedulersProvider: SchedulersProvider,
    @Assisted val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): DeletePatientEffectHandler
  }

  fun build(): ObservableTransformer<DeletePatientEffect, DeletePatientEvent> {
    return RxMobius
        .subtypeEffectHandler<DeletePatientEffect, DeletePatientEvent>()
        .addConsumer(ShowConfirmDeleteDialog::class.java, { uiActions.showConfirmDeleteDialog(it.patientName, it.deletedReason) }, schedulersProvider.ui())
        .addConsumer(ShowConfirmDiedDialog::class.java, { uiActions.showConfirmDiedDialog(it.patientName) }, schedulersProvider.ui())
        .addTransformer(DeletePatient::class.java, deletePatient())
        .addTransformer(MarkPatientAsDead::class.java, markPatientAsDead())
        .build()
  }

  private fun markPatientAsDead(): ObservableTransformer<MarkPatientAsDead, DeletePatientEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .doOnNext { (patientUuid) ->
            patientRepository.updatePatientStatusToDead(patientUuid)
          }
          .map { PatientMarkedAsDead }
    }
  }

  private fun deletePatient(): ObservableTransformer<DeletePatient, DeletePatientEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .observeOn(schedulersProvider.io())
          .doOnNext { (patientUuid, deletedReason) ->
            patientRepository.deletePatient(patientUuid, deletedReason)
          }
          .map { PatientDeleted }
    }
  }
}
