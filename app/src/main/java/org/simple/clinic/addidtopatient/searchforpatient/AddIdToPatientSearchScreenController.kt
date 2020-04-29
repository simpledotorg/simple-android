package org.simple.clinic.addidtopatient.searchforpatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.addidtopatient.searchforpatient.AddIdToPatientSearchValidationError.INPUT_EMPTY
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = AddIdToPatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class AddIdToPatientSearchScreenController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  /**
   * Regular expression that matches digits with interleaved white spaces
   **/
  private val digitsRegex = Regex("[\\s*\\d+]+")

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(InputValidator())
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

  private fun showValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchQueryValidationResult.Invalid>()
        .flatMapIterable { it.validationErrors }
        .doOnNext { Analytics.reportInputValidationError(it.analyticsName) }
        .map {
          { ui: Ui ->
            when (it) {
              INPUT_EMPTY -> ui.setEmptySearchQueryErrorVisible(true)
            }
          }
        }
  }

  private fun resetValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchQueryTextChanged>()
        .map { { ui: Ui -> ui.setEmptySearchQueryErrorVisible(false) } }
  }

  private fun openSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    val inputValidationResultStream = events
        .ofType<SearchQueryValidationResult>()

    val searchClicks = events
        .ofType<SearchClicked>()

    val successfullyValidatedInputStream = Observables
        .combineLatest(searchClicks, inputValidationResultStream) { _, validationResult -> validationResult }
        .ofType<SearchQueryValidationResult.Valid>()

    return successfullyValidatedInputStream
        .map { it.text }
        .map(this::searchCriteriaFromInput)
        .map(this::openSearchResultsBasedOnInput)
  }

  private fun searchCriteriaFromInput(inputString: String): PatientSearchCriteria {
    return when {
      digitsRegex.matches(inputString) -> PatientSearchCriteria.PhoneNumber(inputString.filterNot { it.isWhitespace() })
      else -> PatientSearchCriteria.Name(inputString)
    }
  }

  private fun openSearchResultsBasedOnInput(patientSearchCriteria: PatientSearchCriteria): UiChange {
    return { ui: Ui -> ui.openAddIdToPatientSearchResultsScreen(patientSearchCriteria) }
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
