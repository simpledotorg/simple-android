package org.simple.clinic.shortcodesearchresult

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.plumbing.AsyncOp
import org.simple.clinic.plumbing.BaseUiChangeProducer

typealias ShortCodeSearchResultUiChange = (ShortCodeSearchResultUi) -> Unit

class ShortCodeSearchResultUiChangeProducer(
    uiScheduler: Scheduler
) : BaseUiChangeProducer<ShortCodeSearchResultState, ShortCodeSearchResultUi>(uiScheduler) {
  override fun uiChanges(): ObservableTransformer<ShortCodeSearchResultState, ShortCodeSearchResultUiChange> {
    return ObservableTransformer<ShortCodeSearchResultState, ShortCodeSearchResultUiChange> { states ->
      val showLoading = states
          .filter { it.fetchPatientsAsyncOp == AsyncOp.IN_FLIGHT }
          .map { { ui: ShortCodeSearchResultUi -> ui.showLoading() } }

      val showPatients = states
          .filter { it.fetchPatientsAsyncOp == AsyncOp.SUCCEEDED && it.patients.isNotEmpty() }
          .map { state ->
            { ui: ShortCodeSearchResultUi ->
              ui.hideLoading()
              ui.showSearchResults(state.patients)
              ui.showSearchPatientButton()
            }
          }

      Observable
          .merge(showLoading, showPatients)
    }
  }
}
