package org.simple.clinic.recentpatient

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class AllRecentPatientsEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val patientRepository: PatientRepository,
    @Assisted private val uiActions: AllRecentPatientsUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: AllRecentPatientsUiActions): AllRecentPatientsEffectHandler
  }

  fun build(): ObservableTransformer<AllRecentPatientsEffect, AllRecentPatientsEvent> {
    return RxMobius
        .subtypeEffectHandler<AllRecentPatientsEffect, AllRecentPatientsEvent>()
        .addTransformer(LoadAllRecentPatients::class.java, loadAllRecentPatients())
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientUuid)}, schedulersProvider.ui())
        .build()
  }

  private fun loadAllRecentPatients(): ObservableTransformer<LoadAllRecentPatients, AllRecentPatientsEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .loggedInUser()
                .subscribeOn(schedulersProvider.io())
                .filterAndUnwrapJust()
                .take(1)
                .switchMap(facilityRepository::currentFacility)
                .switchMap { patientRepository.recentPatients(it.uuid) }
                .map(::RecentPatientsLoaded)
          }
    }
  }
}
