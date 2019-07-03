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
import org.simple.clinic.search.PatientSearchValidationError.FULL_NAME_EMPTY
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(validateQuery())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        enableSearchButton(replayedEvents),
        showValidationErrors(replayedEvents),
        resetValidationErrors(replayedEvents),
        openSearchResults(replayedEvents),
        openPatientSummary(replayedEvents)
    )
  }

  private fun enableSearchButton(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchQueryNameChanged>()
        .map { it.name.isNotBlank() }
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

  private fun validateQuery(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val nameChanges = events
          .ofType<SearchQueryNameChanged>()
          .map { it.name.trim() }

      val validationErrors = events.ofType<SearchClicked>()
          .withLatestFrom(nameChanges)
          .map { (_, name) ->
            val errors = mutableListOf<PatientSearchValidationError>()

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
        .flatMapIterable { it.validationErrors }
        .doOnNext { Analytics.reportInputValidationError(it.analyticsName) }
        .map {
          { ui: Ui ->
            when (it) {
              FULL_NAME_EMPTY -> ui.setEmptyFullNameErrorVisible(true)
            }
          }
        }
  }

  private fun resetValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchQueryNameChanged>()
        .map { { ui: Ui -> ui.setEmptyFullNameErrorVisible(false) } }
  }

  private fun openSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    val nameChanges = events
        .ofType<SearchQueryNameChanged>()
        .map { it.name.trim() }

    val validationErrors = events
        .ofType<SearchQueryValidated>()
        .map { it.validationErrors }
        .distinctUntilChanged()

    val searchClicks = events
        .ofType<SearchClicked>()

    return Observables.combineLatest(searchClicks, validationErrors)
        .filter { (_, errors) -> errors.isEmpty() }
        .withLatestFrom(nameChanges) { _, name ->
          { ui: Ui -> ui.openPatientSearchResultsScreen(name) }
        }
  }

  private fun openPatientSummary(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientItemClicked>()
        .map { it.patientUuid }
        .map { patientUuid -> { ui: Ui -> ui.openPatientSummary(patientUuid) } }
  }
}
