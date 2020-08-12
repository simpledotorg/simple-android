package org.simple.clinic.search.results

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientSearchResultsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    @Assisted private val uiActions: PatientSearchResultsUiActions
) {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(uiActions: PatientSearchResultsUiActions): PatientSearchResultsEffectHandler
  }

  fun build(): ObservableTransformer<PatientSearchResultsEffect, PatientSearchResultsEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSearchResultsEffect, PatientSearchResultsEvent>()
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummaryScreen(it.patientUuid) }, schedulers.ui())
        .addConsumer(
            OpenLinkIdWithPatientScreen::class.java,
            { uiActions.openLinkIdWithPatientScreen(it.patientUuid, it.additionalIdentifier) },
            schedulers.ui()
        )
        .addTransformer(SaveNewOngoingPatientEntry::class.java, saveNewPatientEntry())
        .addTransformer(OpenPatientEntryScreen::class.java, openPatientEntryScreen())
        .build()
  }

  private fun saveNewPatientEntry(): ObservableTransformer<SaveNewOngoingPatientEntry, PatientSearchResultsEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { effect ->
            patientRepository
                .saveOngoingEntry(effect.entry)
                .andThen(Observable.just(NewOngoingPatientEntrySaved))
          }
    }
  }

  private fun openPatientEntryScreen(): ObservableTransformer<OpenPatientEntryScreen, PatientSearchResultsEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap {
            userSession
                .loggedInUser()
                .extractIfPresent()
                .switchMap(facilityRepository::currentFacility)
                .take(1)
                .observeOn(schedulers.ui())
                .doOnNext(uiActions::openPatientEntryScreen)
                .switchMap { Observable.empty<PatientSearchResultsEvent>() }
          }
    }
  }
}
