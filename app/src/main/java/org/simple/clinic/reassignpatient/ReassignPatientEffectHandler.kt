package org.simple.clinic.reassignpatient

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.Optional

class ReassignPatientEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<ReassignPatientViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<ReassignPatientViewEffect>
    ): ReassignPatientEffectHandler
  }

  fun build(): ObservableTransformer<ReassignPatientEffect, ReassignPatientEvent> = RxMobius
      .subtypeEffectHandler<ReassignPatientEffect, ReassignPatientEvent>()
      .addTransformer(LoadAssignedFacility::class.java, loadAssignedFacility())
      .addTransformer(ChangeAssignedFacility::class.java, changeAssignedFacility())
      .addConsumer(ReassignPatientViewEffect::class.java, viewEffectsConsumer::accept)
      .build()

  private fun loadAssignedFacility(): ObservableTransformer<LoadAssignedFacility, ReassignPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map(::getAssignedFacility)
          .map(::AssignedFacilityLoaded)
    }
  }

  private fun getAssignedFacility(patient: Patient): Optional<Facility> {
    return Optional
        .ofNullable(patient.assignedFacilityId)
        .flatMap { facilityRepository.facility(it) }
  }

  private fun changeAssignedFacility(): ObservableTransformer<ChangeAssignedFacility, ReassignPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { (patientUuid, assignedFacilityId) ->
            patientRepository.updateAssignedFacilityId(patientUuid, assignedFacilityId)
          }
          .map { AssignedFacilityChanged }
    }
  }
}

