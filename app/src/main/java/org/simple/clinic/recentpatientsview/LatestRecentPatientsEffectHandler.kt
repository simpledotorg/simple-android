package org.simple.clinic.recentpatientsview

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class LatestRecentPatientsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val currentFacility: Lazy<Facility>,
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
          .observeOn(schedulers.io())
          .switchMap { effect ->
            patientRepository
                .recentPatients(currentFacility.get().uuid, effect.count)
                .map(::RecentPatientsLoaded)
          }
    }
  }
}
