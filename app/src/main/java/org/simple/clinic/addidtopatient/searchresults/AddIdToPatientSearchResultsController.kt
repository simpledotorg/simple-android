package org.simple.clinic.addidtopatient.searchresults

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = AddIdToPatientSearchResultsScreen
typealias UiChange = (Ui) -> Unit

class AddIdToPatientSearchResultsController @Inject constructor(
    private val patientRepository: PatientRepository
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
      events.ofType<AddIdToPatientSearchResultClicked>()
          .map { it.searchResult.uuid }
          .map { patientUuid ->
            { ui: Ui -> ui.openPatientSummaryScreen(patientUuid) }
          }

  private fun registerNewPatient(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events.ofType<AddIdToPatientSearchResultsScreenCreated>()

    return events.ofType<AddIdToPatientSearchResultRegisterNewPatientClicked>()
        .withLatestFrom(screenCreates) { _, screenCreated -> screenCreated.searchCriteria to screenCreated.identifier }
        .map { (criteria, identifier) -> entryFromSearchCriteria(criteria, identifier) }
        .flatMap(this::saveEntryAndGoToEntryScreen)
  }

  private fun entryFromSearchCriteria(
      criteria: PatientSearchCriteria,
      identifier: Identifier
  ): OngoingNewPatientEntry {
    val ongoingNewPatientEntry = when (criteria) {
      is Name -> OngoingNewPatientEntry.fromFullName(criteria.patientName)
      is PhoneNumber -> OngoingNewPatientEntry.fromPhoneNumber(criteria.phoneNumber)
    }

    return ongoingNewPatientEntry.withIdentifier(identifier)
  }

  private fun saveEntryAndGoToEntryScreen(entry: OngoingNewPatientEntry): Observable<UiChange> {
    return patientRepository
        .saveOngoingEntry(entry)
        .andThen(Observable.just { ui: Ui -> ui.openPatientEntryScreen() })
  }
}
