package org.simple.clinic.editpatient.deletepatient

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class DeletePatientEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions,
    @Assisted private val viewEffectsConsumer: Consumer<DeletePatientViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: UiActions,
        viewEffectsConsumer: Consumer<DeletePatientViewEffect>
    ): DeletePatientEffectHandler
  }

  fun build(): ObservableTransformer<DeletePatientEffect, DeletePatientEvent> {
    return RxMobius
        .subtypeEffectHandler<DeletePatientEffect, DeletePatientEvent>()
        .addTransformer(DeletePatient::class.java, deletePatient())
        .addTransformer(MarkPatientAsDead::class.java, markPatientAsDead())
        .addTransformer(LoadPatient::class.java, loadPatient())
        .addConsumer(DeletePatientViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadPatient(): ObservableTransformer<LoadPatient, DeletePatientEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .map {
            val patient = patientRepository.patientImmediate(it.patientUuid)
            PatientLoaded(patient!!)
          }
    }
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
