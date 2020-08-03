package org.simple.clinic.search

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent

private typealias Ui = PatientSearchUi
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @AssistedInject constructor(
    @Assisted private val additionalIdentifier: Identifier?
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(additionalIdentifier: Identifier?): PatientSearchScreenController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(InputValidator())
        .replay()

    return Observable.mergeArray(
        openPatientSummary(replayedEvents),
        toggleAllPatientsVisibility(replayedEvents),
        toggleSearchButtonVisibility(replayedEvents)
    )
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
