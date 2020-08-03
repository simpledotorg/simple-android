package org.simple.clinic.summary.prescribeddrugs

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class DrugSummaryEffectHandler @AssistedInject constructor(
    private val prescriptionRepository: PrescriptionRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: DrugSummaryUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: DrugSummaryUiActions): DrugSummaryEffectHandler
  }

  fun build(): ObservableTransformer<DrugSummaryEffect, DrugSummaryEvent> = RxMobius
      .subtypeEffectHandler<DrugSummaryEffect, DrugSummaryEvent>()
      .addTransformer(LoadPrescribedDrugs::class.java, populatePrescribedDrugs())
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .build()

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, DrugSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { userSession.loggedInUserImmediate() }
          .map { facilityRepository.currentFacilityImmediate(it) }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun populatePrescribedDrugs(): ObservableTransformer<LoadPrescribedDrugs, DrugSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { prescriptionRepository.newestPrescriptionsForPatient(patientUuid = it.patientUuid) }
          .map(::PrescribedDrugsLoaded)
    }
  }
}
