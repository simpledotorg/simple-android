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
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.Optional
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
      .addTransformer(ChangeAssignedFacility::class.java, changeAssignedFacility())
      .addAction(NotifyAssignedFacilityChanged::class.java, uiActions::notifyAssignedFacilityChanged, schedulersProvider.ui())
      .build()

  private fun changeAssignedFacility(): ObservableTransformer<ChangeAssignedFacility, AssignedFacilityEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { (patientUuid, assignedFacilityId) ->
            patientRepository.updateAssignedFacilityId(patientUuid, assignedFacilityId)
          }
          .map { AssignedFacilityChanged }
    }
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
