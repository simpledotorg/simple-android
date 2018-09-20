package org.simple.clinic.search.results

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.DATE_OF_BIRTH_FORMAT_FOR_UI
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.OngoingPatientEntry.PersonalDetails
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import javax.inject.Inject

typealias Ui = PatientSearchResultsScreen
typealias UiChange = (Ui) -> Unit

class PatientSearchResultsController @Inject constructor(
    private val repository: PatientRepository,
    private val clock: Clock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        showComputedAge(replayedEvents),
        populateSearchResults(replayedEvents),
        openPatientSummary(replayedEvents),
        createNewPatient(replayedEvents))
  }

  private fun showComputedAge(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSearchResultsScreenCreated>()
        .map { it.key }
        .map { (_, age, dob) ->
          val computedAge = when {
            age.isNotBlank() -> age.trim().toInt()
            else -> calculateAgeFromDateOfBirth(dob)
          }
          computedAge
        }
        .map { { ui: Ui -> ui.showComputedAge(it.toString()) } }
  }

  private fun populateSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSearchResultsScreenCreated>()
        .map { it.key }
        .map { (name, age, dob) ->
          val computedAge = when {
            age.isNotBlank() -> age.trim().toInt()
            else -> calculateAgeFromDateOfBirth(dob)
          }
          name to computedAge
        }
        .flatMap { (name, computedAge) ->
          repository.search(name, computedAge, includeFuzzyNameSearch = true) }
        .map {
          { ui: Ui ->
            ui.updateSearchResults(it)
            ui.setEmptyStateVisible(it.isEmpty())
          }
        }
  }

  private fun calculateAgeFromDateOfBirth(dob: String): Int {
    val dateOfBirth = DATE_OF_BIRTH_FORMAT_FOR_UI.parse(dob.trim(), LocalDate::from)
    return Period.between(dateOfBirth, LocalDate.now(clock)).years
  }

  private fun openPatientSummary(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSearchResultClicked>()
        .map { it.searchResult.uuid }
        .map { { ui: Ui -> ui.openPatientSummaryScreen(patientUuid = it) } }
  }

  private fun createNewPatient(events: Observable<UiEvent>): Observable<UiChange> {
    val screenKey = events
        .ofType<PatientSearchResultsScreenCreated>()
        .map { it.key }

    return events.ofType<CreateNewPatientClicked>()
        .withLatestFrom(screenKey)
        .map { (_, key) ->
          OngoingPatientEntry(PersonalDetails(
              fullName = key.fullName,
              dateOfBirth = key.dateOfBirth,
              age = key.age,
              gender = null))
        }
        .flatMap {
          repository
              .saveOngoingEntry(it)
              .andThen(Observable.just({ ui: Ui -> ui.openPatientEntryScreen() }))
        }
  }
}
