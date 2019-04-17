package org.simple.clinic.addidtopatient.searchresults

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.patient.PatientRepository
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
        .withLatestFrom(screenCreates) { _, screenCreated -> screenCreated.patientName to screenCreated.identifier }
        .map { (patientName, identifier) ->
          OngoingNewPatientEntry(
              personalDetails = PersonalDetails(
                  fullName = patientName,
                  dateOfBirth = null,
                  age = null,
                  gender = null),
              identifier = identifier)
        }
        .flatMap {
          patientRepository
              .saveOngoingEntry(it)
              .andThen(Observable.just({ ui: Ui -> ui.openPatientEntryScreen() }))
        }
  }
}
