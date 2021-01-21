package org.simple.clinic.recentpatient

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class AllRecentPatientsEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val currentFacility: Lazy<Facility>,
    @Assisted private val uiActions: AllRecentPatientsUiActions
) {

  @AssistedFactory
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
          .observeOn(schedulersProvider.io())
          .switchMap {
            patientRepository
                .recentPatients(currentFacility.get().uuid)
                .map(::RecentPatientsLoaded)
          }
    }
  }
}
