package org.simple.clinic.shortcodesearchresult

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
      states
          .filter { it.fetchPatientsAsyncOp == AsyncOp.IN_FLIGHT }
          .map { { ui: ShortCodeSearchResultUi -> ui.showLoading() } }
    }
  }
}
