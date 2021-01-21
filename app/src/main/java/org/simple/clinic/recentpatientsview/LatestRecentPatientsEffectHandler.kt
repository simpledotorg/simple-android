package org.simple.clinic.recentpatientsview

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class LatestRecentPatientsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val currentFacilityChanges: Observable<Facility>,
    @Assisted private val uiActions: LatestRecentPatientsUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: LatestRecentPatientsUiActions): LatestRecentPatientsEffectHandler
  }

  fun build(): ObservableTransformer<LatestRecentPatientsEffect, LatestRecentPatientsEvent> {
    return RxMobius
        .subtypeEffectHandler<LatestRecentPatientsEffect, LatestRecentPatientsEvent>()
        .addTransformer(LoadRecentPatients::class.java, loadRecentPatients())
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientUuid) }, schedulers.ui())
        .addAction(OpenAllRecentPatientsScreen::class.java, uiActions::openRecentPatientsScreen, schedulers.ui())
        .build()
  }

  private fun loadRecentPatients(): ObservableTransformer<LoadRecentPatients, LatestRecentPatientsEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { effect ->
            currentFacilityChanges
                .subscribeOn(schedulers.io())
                .switchMap { patientRepository.recentPatients(it.uuid, effect.count) }
                .map(::RecentPatientsLoaded)
          }
    }
  }
}
