package org.simple.clinic.addidtopatient.searchforpatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.addidtopatient.searchforpatient.AddIdToPatientSearchValidationError.INPUT_EMPTY
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = AddIdToPatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class AddIdToPatientSearchScreenController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .compose(validateQuery())
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
      val nameChanges = events
          .ofType<SearchQueryTextChanged>()
          .map { it.text.trim() }

      val validationErrors = events.ofType<SearchClicked>()
          .withLatestFrom(nameChanges)
          .map { (_, name) ->
            val errors = mutableListOf<AddIdToPatientSearchValidationError>()

            if (name.isBlank()) {
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
    val nameChanges = events
        .ofType<SearchQueryTextChanged>()
        .map { it.text.trim() }

    val validationErrors = events
        .ofType<SearchQueryValidated>()
        .map { it.validationErrors }
        .distinctUntilChanged()

    val searchClicks = events
        .ofType<SearchClicked>()

    return Observables.combineLatest(searchClicks, validationErrors)
        .filter { (_, errors) -> errors.isEmpty() }
        .withLatestFrom(nameChanges) { _, name ->
          { ui: Ui -> ui.openAddIdToPatientSearchResultsScreen(name) }
        }
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
