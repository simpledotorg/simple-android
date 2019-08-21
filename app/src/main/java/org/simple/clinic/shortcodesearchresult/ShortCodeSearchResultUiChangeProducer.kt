package org.simple.clinic.shortcodesearchresult

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.plumbing.AsyncOp.IN_FLIGHT
import org.simple.clinic.plumbing.AsyncOp.SUCCEEDED
import org.simple.clinic.plumbing.BaseUiChangeProducer

typealias ShortCodeSearchResultUiChange = (ShortCodeSearchResultUi) -> Unit

class ShortCodeSearchResultUiChangeProducer(
    uiScheduler: Scheduler
) : BaseUiChangeProducer<ShortCodeSearchResultState, ShortCodeSearchResultUi>(uiScheduler) {
  override fun uiChanges(): ObservableTransformer<ShortCodeSearchResultState, ShortCodeSearchResultUiChange> {
    return ObservableTransformer { states ->
      Observable.merge(
          showLoading(states),
          showPatients(states),
          showNoPatientsMatched(states)
      )
    }
  }

  private fun showLoading(
      states: Observable<ShortCodeSearchResultState>
  ): Observable<(ShortCodeSearchResultUi) -> Unit> {
    return states
        .filter { it.fetchPatientsAsyncOp == IN_FLIGHT }
        .map { { ui: ShortCodeSearchResultUi -> ui.showLoading() } }
  }

  private fun showPatients(
      states: Observable<ShortCodeSearchResultState>
  ): Observable<(ShortCodeSearchResultUi) -> Unit> {
    return states
        .filter { it.fetchPatientsAsyncOp == SUCCEEDED && it.patients.isNotEmpty() }
        .map { state ->
          { ui: ShortCodeSearchResultUi ->
            with(ui) {
              hideLoading()
              showSearchPatientButton()
              showSearchResults(state.patients)
            }
          }
        }
  }

  private fun showNoPatientsMatched(
      states: Observable<ShortCodeSearchResultState>
  ): Observable<(ShortCodeSearchResultUi) -> Unit> {
    return states
        .filter { it.fetchPatientsAsyncOp == SUCCEEDED && it.patients.isEmpty() }
        .map {
          { ui: ShortCodeSearchResultUi ->
            with(ui) {
              hideLoading()
              showSearchPatientButton()
              showNoPatientsMatched()
            }
          }
        }
  }
}
