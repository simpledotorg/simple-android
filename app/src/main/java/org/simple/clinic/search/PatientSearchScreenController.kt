package org.simple.clinic.search

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @Inject constructor(
    private val repository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        enableSearchButton(replayedEvents),
        switchBetweenDateOfBirthAndAge(replayedEvents),
        openSearchResults(replayedEvents),
        saveAndProceeds(replayedEvents))
  }

  private fun enableSearchButton(events: Observable<UiEvent>): Observable<UiChange> {
    val nameChanges = events
        .ofType<SearchQueryNameChanged>()
        .map { it.name }

    val ageChanges = events
        .ofType<SearchQueryAgeChanged>()
        .map { it.ageString }

    return Observables.combineLatest(nameChanges, ageChanges)
        .map { (name, age) -> name.isNotBlank() && age.isNotBlank() }
        .map { isQueryComplete ->
          { ui: Ui ->
            if (isQueryComplete) {
              ui.showSearchButtonAsEnabled()
            } else {
              ui.showSearchButtonAsDisabled()
            }
          }
        }
  }

  private fun switchBetweenDateOfBirthAndAge(events: Observable<UiEvent>): Observable<UiChange> {
    val isDateOfBirthBlanks = events
        .ofType<SearchQueryDateOfBirthChanged>()
        .map { it.dateOfBirth.isBlank() }

    val isAgeBlanks = events
        .ofType<SearchQueryAgeChanged>()
        .map { it.ageString.isBlank() }

    return Observables.combineLatest(isDateOfBirthBlanks, isAgeBlanks)
        .distinctUntilChanged()
        .map<UiChange> { (dateBlank, ageBlank) ->
          when {
            !dateBlank && ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE) }
            dateBlank && !ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(AGE_VISIBLE) }
            dateBlank && ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(BOTH_VISIBLE) }
            else -> throw AssertionError("Both date-of-birth and age cannot have user input at the same time")
          }
        }
  }

  private fun openSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    val nameChanges = events
        .ofType<SearchQueryNameChanged>()
        .map { it.name.trim() }

    val ageChanges = events
        .ofType<SearchQueryAgeChanged>()
        .map { it.ageString }

    return events
        .ofType<SearchClicked>()
        .withLatestFrom(nameChanges, ageChanges)
        .filter { (_, name, age) -> name.isNotBlank() && age.isNotBlank() }
        .map { { ui: Ui -> ui.openPatientSearchResultsScreen() } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val nameChanges = events
        .ofType<SearchQueryNameChanged>()
        .map { it.name.trim() }

    return events
        .ofType<CreateNewPatientClicked>()
        .withLatestFrom(nameChanges) { _, name -> name }
        .take(1)
        .map { OngoingPatientEntry(personalDetails = OngoingPatientEntry.PersonalDetails(it, null, null, null)) }
        .flatMapCompletable { newEntry -> repository.saveOngoingEntry(newEntry) }
        .andThen(Observable.just { ui: Ui -> ui.openPatientEntryScreen() })
  }
}
