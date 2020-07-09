package org.simple.clinic.recentpatientsview

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class LatestRecentPatientsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val patientRepository: PatientRepository,
    @Assisted private val uiActions: LatestRecentPatientsUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: LatestRecentPatientsUiActions): LatestRecentPatientsEffectHandler
  }

  fun build(): ObservableTransformer<LatestRecentPatientsEffect, LatestRecentPatientsEvent> {
    return RxMobius
        .subtypeEffectHandler<LatestRecentPatientsEffect, LatestRecentPatientsEvent>()
        .addTransformer(LoadRecentPatients::class.java, loadRecentPatients())
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientUuid)}, schedulers.ui())
        .addAction(OpenAllRecentPatientsScreen::class.java, uiActions::openRecentPatientsScreen, schedulers.ui())
        .build()
  }

  private fun loadRecentPatients(): ObservableTransformer<LoadRecentPatients, LatestRecentPatientsEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { effect ->
            userSession
                .loggedInUser()
                .subscribeOn(schedulers.io())
                .filterAndUnwrapJust()
                .take(1)
                .switchMap(facilityRepository::currentFacility)
                .switchMap { facility -> patientRepository.recentPatients(facility.uuid, effect.count) }
                .map(::RecentPatientsLoaded)
          }
    }
  }
}
