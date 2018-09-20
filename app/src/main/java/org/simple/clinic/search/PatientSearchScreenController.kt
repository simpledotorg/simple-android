package org.simple.clinic.search

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.newentry.DateOfBirthFormatValidator
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.search.PatientSearchValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.search.PatientSearchValidationError.BOTH_DATEOFBIRTH_AND_AGE_PRESENT
import org.simple.clinic.search.PatientSearchValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.search.PatientSearchValidationError.FULL_NAME_EMPTY
import org.simple.clinic.search.PatientSearchValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.search.results.CreateNewPatientClicked
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @Inject constructor(
    private val repository: PatientRepository,
    private val dobValidator: DateOfBirthFormatValidator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events
        .compose(ReportAnalyticsEvents())
        .replay()
        .refCount()
        .compose(validateQuery())

    return Observable.mergeArray(
        enableSearchButton(replayedEvents),
        switchBetweenDateOfBirthAndAge(replayedEvents),
        showValidationErrors(replayedEvents),
        resetValidationErrors(replayedEvents),
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

  private fun validateQuery(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val nameChanges = events
          .ofType<SearchQueryNameChanged>()
          .map { it.name.trim() }

      val ageChanges = events
          .ofType<SearchQueryAgeChanged>()
          .map { it.ageString.trim() }

      val dateOfBirthChanges = events
          .ofType<SearchQueryDateOfBirthChanged>()
          .map { it.dateOfBirth.trim() }

      val validationErrors = events.ofType<SearchClicked>()
          .withLatestFrom(nameChanges, ageChanges, dateOfBirthChanges) { _, name, age, dob -> Triple(name, age, dob) }
          .map { (name, age, dateOfBirth) ->
            val errors = mutableListOf<PatientSearchValidationError>()

            if (dateOfBirth.isNullOrBlank() && age.isNullOrBlank()) {
              errors += BOTH_DATEOFBIRTH_AND_AGE_ABSENT

            } else if (dateOfBirth?.isNotBlank() == true && age?.isNotBlank() == true) {
              errors += BOTH_DATEOFBIRTH_AND_AGE_PRESENT

            } else if (dateOfBirth.isNotBlank()) {
              val dobValidationResult = dobValidator.validate(dateOfBirth)
              errors += when (dobValidationResult) {
                DateOfBirthFormatValidator.Result.INVALID_PATTERN -> listOf(INVALID_DATE_OF_BIRTH)
                DateOfBirthFormatValidator.Result.DATE_IS_IN_FUTURE -> listOf(DATE_OF_BIRTH_IN_FUTURE)
                DateOfBirthFormatValidator.Result.VALID -> listOf()
              }
            }
            if (name.isBlank()) {
              errors += FULL_NAME_EMPTY
            }
            SearchQueryValidated(errors)
          }

      events.mergeWith(validationErrors)
    }
  }

  private fun showValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<SearchQueryValidated>()
        .distinctUntilChanged()
        .flatMapIterable { it.validationErrors }
        .doOnNext { Analytics.reportInputValidationError(it.analyticsName) }
        .map {
          { ui: Ui ->
            when (it) {
              FULL_NAME_EMPTY -> ui.setEmptyFullNameErrorVisible(true)
              BOTH_DATEOFBIRTH_AND_AGE_ABSENT -> ui.setEmptyDateOfBirthAndAgeErrorVisible(true)
              INVALID_DATE_OF_BIRTH -> ui.setInvalidDateOfBirthErrorVisible(true)
              DATE_OF_BIRTH_IN_FUTURE -> ui.setDateOfBirthIsInFutureErrorVisible(true)

              BOTH_DATEOFBIRTH_AND_AGE_PRESENT -> {
                throw AssertionError("Should never receive $it")
              }
            }
          }
        }
  }

  private fun resetValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    val nameErrorResets = events
        .ofType<SearchQueryNameChanged>()
        .map { { ui: Ui -> ui.setEmptyFullNameErrorVisible(false) } }

    val ageErrorResets = events
        .ofType<SearchQueryAgeChanged>()
        .map { { ui: Ui -> ui.setEmptyDateOfBirthAndAgeErrorVisible(false) } }

    val dateOfBirthErrorResets = events
        .ofType<SearchQueryDateOfBirthChanged>()
        .map {
          { ui: Ui ->
            ui.setEmptyDateOfBirthAndAgeErrorVisible(false)
            ui.setInvalidDateOfBirthErrorVisible(false)
            ui.setDateOfBirthIsInFutureErrorVisible(false)
          }
        }

    return Observable.merge(nameErrorResets, dateOfBirthErrorResets, ageErrorResets)
  }

  private fun openSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    val nameChanges = events
        .ofType<SearchQueryNameChanged>()
        .map { it.name.trim() }

    val ageChanges = events
        .ofType<SearchQueryAgeChanged>()
        .map { it.ageString.trim() }

    val dateOfBirthChanges = events
        .ofType<SearchQueryDateOfBirthChanged>()
        .map { it.dateOfBirth.trim() }

    val validationErrors = events
        .ofType<SearchQueryValidated>()
        .map { it.validationErrors }
        .distinctUntilChanged()

    val searchClicks = events
        .ofType<SearchClicked>()

    return Observables.combineLatest(searchClicks, validationErrors)
        .filter { (_, errors) -> errors.isEmpty() }
        .withLatestFrom(nameChanges, ageChanges, dateOfBirthChanges) { _, name, age, dob -> Triple(name, age, dob) }
        .map { (name, age, dateOfBirth) -> { ui: Ui -> ui.openPatientSearchResultsScreen(name, age, dateOfBirth) } }
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
