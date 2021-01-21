package org.simple.clinic.summary.assignedfacility

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.function.Function

class AssignedFacilityEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UiActions): AssignedFacilityEffectHandler
  }

  fun build(): ObservableTransformer<AssignedFacilityEffect, AssignedFacilityEvent> = RxMobius
      .subtypeEffectHandler<AssignedFacilityEffect, AssignedFacilityEvent>()
      .addTransformer(LoadAssignedFacility::class.java, loadAssignedFacility())
      .addConsumer(ChangeAssignedFacility::class.java, { changeAssignedFacility(it) }, schedulersProvider.io())
      .addAction(OpenFacilitySelection::class.java, uiActions::openFacilitySelection, schedulersProvider.ui())
      .build()

  private fun changeAssignedFacility(effect: ChangeAssignedFacility) {
    val (patientUuid, assignedFacilityId) = effect
    patientRepository.updateAssignedFacilityId(patientUuid, assignedFacilityId)
  }

  private fun loadAssignedFacility(): ObservableTransformer<LoadAssignedFacility, AssignedFacilityEvent> {
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
        .flatMap(Function { facilityRepository.facility(it) })
  }
}
