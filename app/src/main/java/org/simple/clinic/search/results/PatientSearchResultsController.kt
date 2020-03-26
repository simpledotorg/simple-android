package org.simple.clinic.search.results

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject
import javax.inject.Named

typealias Ui = PatientSearchResultsScreen
typealias UiChange = (Ui) -> Unit

class PatientSearchResultsController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    @Named("is_facility_switched") private val isFacilitySwitchedPreference: Preference<Boolean>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        openPatientSummary(replayedEvents),
        registerNewPatient(replayedEvents)
    )
  }

  private fun openPatientSummary(events: Observable<UiEvent>): ObservableSource<UiChange> =
      events.ofType<PatientSearchResultClicked>()
          .map { it.patientUuid }
          .map { patientUuid ->
            { ui: Ui -> ui.openPatientSummaryScreen(patientUuid) }
          }

  private fun registerNewPatient(events: Observable<UiEvent>): Observable<UiChange> {
    val currentFacility = events
        .ofType<PatientSearchResultRegisterNewPatient>()
        .flatMap { userSession.loggedInUser() }
        .filterAndUnwrapJust()
        .switchMap { facilityRepository.currentFacility(it) }

    return events
        .ofType<PatientSearchResultRegisterNewPatient>()
        .map { it.searchCriteria }
        .map(this::createOngoingEntryFromSearchCriteria)
        .zipWith(currentFacility)
        .flatMap { (ongoingNewPatientEntry, currentFacility) ->
          saveEntryAndGoToRegisterPatientScreen(ongoingNewPatientEntry, currentFacility)
        }
  }

  private fun createOngoingEntryFromSearchCriteria(searchCriteria: PatientSearchCriteria): OngoingNewPatientEntry {
    return when (searchCriteria) {
      is Name -> OngoingNewPatientEntry.fromFullName(searchCriteria.patientName)
      is PhoneNumber -> OngoingNewPatientEntry.fromPhoneNumber(searchCriteria.phoneNumber)
    }
  }

  private fun saveEntryAndGoToRegisterPatientScreen(ongoingNewPatientEntry: OngoingNewPatientEntry, currentFacility: Facility): Observable<UiChange> {
    val nextScreenChange = if (isFacilitySwitchedPreference.get()) {
      { ui: Ui -> ui.openAlertFacilityChangeSheet(currentFacility) }
    } else {
      { ui: Ui -> ui.openPatientEntryScreen() }
    }

    return patientRepository
        .saveOngoingEntry(ongoingNewPatientEntry)
        .andThen(Observable.just(nextScreenChange))
  }
}
