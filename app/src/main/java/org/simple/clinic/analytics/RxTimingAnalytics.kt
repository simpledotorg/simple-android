package org.simple.clinic.analytics

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.util.minus

class RxTimingAnalytics<T>(
    private val analyticsName: String,
    private val timestampScheduler: Scheduler
) : ObservableTransformer<T, T> {

  override fun apply(upstream: Observable<T>): ObservableSource<T> {
    return Observable.just(upstream)
        .timestamp(timestampScheduler)
        .flatMap { start ->
          start
              .value()
              .timestamp(timestampScheduler)
              .doOnNext { end ->
                val timeRequired = (end - start)
                Analytics.reportTimeTaken(analyticsName, timeRequired)
              }
              .map { it.value() }
        }
  }
}
