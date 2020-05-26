package org.simple.clinic

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.widgets.UiEvent

class ReportAnalyticsEvents : ObservableTransformer<UiEvent, UiEvent> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiEvent> {
    return events.doOnNext {
      if (it.analyticsName.isNotBlank()) {
        Analytics.reportUserInteraction(it.analyticsName)
      }
    }
  }
}
