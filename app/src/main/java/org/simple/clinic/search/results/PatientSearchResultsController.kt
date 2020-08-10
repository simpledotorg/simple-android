package org.simple.clinic.search.results

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent

typealias Ui = PatientSearchResultsUi
typealias UiChange = (Ui) -> Unit

class PatientSearchResultsController @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    @Assisted private val patientSearchCriteria: PatientSearchCriteria
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(patientSearchCriteria: PatientSearchCriteria): PatientSearchResultsController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.mergeArray(
        openPatientSummary(replayedEvents),
        registerNewPatient(replayedEvents),
        openLinkIdWithPatientScreen(replayedEvents)
    )
  }

  private fun openPatientSummary(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val additionalIdentifierStream = events
        .ofType<PatientSearchResultsScreenCreated>()
        .map { patientSearchCriteria.additionalIdentifier.toOptional() }

    return events
        .ofType<PatientSearchResultClicked>()
        .withLatestFrom(additionalIdentifierStream)
        .filter { (_, additionalIdentifier) -> !additionalIdentifier.isPresent() }
        .map { (clickedResult, _) ->
          { ui: Ui ->
            ui.openPatientSummaryScreen(clickedResult.patientUuid)
          }
        }
  }

  private fun registerNewPatient(events: Observable<UiEvent>): Observable<UiChange> {
    val currentFacility = events
        .ofType<PatientSearchResultRegisterNewPatient>()
        .flatMap { userSession.loggedInUser() }
        .filterAndUnwrapJust()
        .switchMap { facilityRepository.currentFacility(it) }

    return events
        .ofType<PatientSearchResultRegisterNewPatient>()
        .map { createOngoingEntryFromSearchCriteria(patientSearchCriteria) }
        .zipWith(currentFacility)
        .flatMap { (ongoingNewPatientEntry, currentFacility) ->
          saveEntryAndGoToRegisterPatientScreen(ongoingNewPatientEntry, currentFacility)
        }
  }

  private fun createOngoingEntryFromSearchCriteria(searchCriteria: PatientSearchCriteria): OngoingNewPatientEntry {
    var ongoingNewPatientEntry = when (searchCriteria) {
      is Name -> OngoingNewPatientEntry.fromFullName(searchCriteria.patientName)
      is PhoneNumber -> OngoingNewPatientEntry.fromPhoneNumber(searchCriteria.phoneNumber)
    }

    if (searchCriteria.additionalIdentifier != null) {
      ongoingNewPatientEntry = ongoingNewPatientEntry.withIdentifier(searchCriteria.additionalIdentifier)
    }

    return ongoingNewPatientEntry
  }

  private fun saveEntryAndGoToRegisterPatientScreen(ongoingNewPatientEntry: OngoingNewPatientEntry, currentFacility: Facility): Observable<UiChange> {
    return patientRepository
        .saveOngoingEntry(ongoingNewPatientEntry)
        .andThen(Observable.just { ui: Ui -> ui.openPatientEntryScreen(currentFacility) })
  }

  private fun openLinkIdWithPatientScreen(events: Observable<UiEvent>): Observable<UiChange> {
    val additionalIdentifierStream = events
        .ofType<PatientSearchResultsScreenCreated>()
        .map { patientSearchCriteria.additionalIdentifier.toOptional() }

    return events
        .ofType<PatientSearchResultClicked>()
        .withLatestFrom(additionalIdentifierStream)
        .filter { (_, additionalIdentifier) -> additionalIdentifier.isPresent() }
        .map { (clickedResult, additionalIdentifier) ->
          { ui: Ui ->
            ui.openLinkIdWithPatientScreen(clickedResult.patientUuid, additionalIdentifier.get())
          }
        }
  }
}
