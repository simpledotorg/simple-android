package org.simple.clinic.search

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.search.PatientSearchValidationError.INPUT_EMPTY
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  /**
   * Regular expression that matches digits with interleaved white spaces
   **/
  private val digitsRegex = Regex("[\\s*\\d+]+")

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(validateQuery())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        showValidationErrors(replayedEvents),
        resetValidationErrors(replayedEvents),
        openSearchResults(replayedEvents),
        openPatientSummary(replayedEvents),
        toggleAllPatientsVisibility(replayedEvents),
        toggleSearchButtonVisibility(replayedEvents)
    )
  }

  private fun validateQuery(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val textChanges = events
          .ofType<SearchQueryTextChanged>()
          .map { it.text.trim() }

      val validationErrors = events.ofType<SearchClicked>()
          .withLatestFrom(textChanges)
          .map { (_, text) ->
            val errors = mutableListOf<PatientSearchValidationError>()

            if (text.isBlank()) {
              errors += INPUT_EMPTY
            }
            SearchQueryValidated(errors)
          }

      events.mergeWith(validationErrors)
    }
  }

  private fun showValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<SearchQueryValidated>()
        .flatMapIterable { it.validationErrors }
        .doOnNext { Analytics.reportInputValidationError(it.analyticsName) }
        .map {
          { ui: Ui ->
            when (it) {
              INPUT_EMPTY -> ui.setEmptyTextFieldErrorVisible(true)
            }
          }
        }
  }

  private fun resetValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchQueryTextChanged>()
        .map { { ui: Ui -> ui.setEmptyTextFieldErrorVisible(false) } }
  }

  private fun openSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    val validationErrors = events
        .ofType<SearchQueryValidated>()
        .map { it.validationErrors }
        .distinctUntilChanged()

    val searchClicks = events
        .ofType<SearchClicked>()

    val textChanges = events
        .ofType<SearchQueryTextChanged>()
        .map { it.text.trim() }

    return Observables.combineLatest(searchClicks, validationErrors)
        .filter { (_, errors) -> errors.isEmpty() }
        .withLatestFrom(textChanges) { _, text -> text }
        .map(this::searchCriteriaFromInput)
        .map(this::openSearchResultsBasedOnInput)
  }

  private fun searchCriteriaFromInput(inputString: String): PatientSearchCriteria {
    return when {
      digitsRegex.matches(inputString) -> PhoneNumber(inputString.filterNot { it.isWhitespace() })
      else -> Name(inputString)
    }
  }

  private fun openSearchResultsBasedOnInput(patientSearchCriteria: PatientSearchCriteria): UiChange {
    return { ui: Ui -> ui.openSearchResultsScreen(patientSearchCriteria) }
  }

  private fun openPatientSummary(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientItemClicked>()
        .map { it.patientUuid }
        .map { patientUuid -> { ui: Ui -> ui.openPatientSummary(patientUuid) } }
  }

  private fun toggleAllPatientsVisibility(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events
        .ofType<SearchQueryTextChanged>()
        .map { it.text.isNotBlank() }
        .map { isSearchQueryPresent ->
          { ui: Ui ->
            if (isSearchQueryPresent) {
              ui.hideAllPatientsInFacility()
            } else {
              ui.showAllPatientsInFacility()
            }
          }
        }
  }

  private fun toggleSearchButtonVisibility(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events
        .ofType<SearchQueryTextChanged>()
        .map { it.text.isNotBlank() }
        .map { isSearchQueryPresent ->
          { ui: Ui ->
            if (isSearchQueryPresent) {
              ui.showSearchButton()
            } else {
              ui.hideSearchButton()
            }
          }
        }
  }
}
