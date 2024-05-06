package org.simple.clinic.reassignPatient

import com.spotify.mobius.rx2.RxMobius
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
) {

  fun build(): ObservableTransformer<ReassignPatientEffect, ReassignPatientEvent> = RxMobius
      .subtypeEffectHandler<ReassignPatientEffect, ReassignPatientEvent>()
      .addTransformer(LoadAssignedFacility::class.java, loadAssignedFacility())
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
}

