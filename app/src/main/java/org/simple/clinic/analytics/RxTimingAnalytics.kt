package org.simple.clinic.analytics

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.util.minus
import java.util.concurrent.atomic.AtomicBoolean

class RxTimingAnalytics<T>(
    private val analyticsName: String,
    private val timestampScheduler: Scheduler
) : ObservableTransformer<T, T> {

  override fun apply(upstream: Observable<T>): ObservableSource<T> {
    return Observable.just(upstream)
        .timestamp(timestampScheduler)
        .flatMap { start ->
          val alreadyReportedToAnalytics = AtomicBoolean(false)

          start
              .value()
              .timestamp(timestampScheduler)
              .doOnNext { end ->
                if(!alreadyReportedToAnalytics.get()) {
                  val timeTaken = (end - start)
                  Analytics.reportTimeTaken(analyticsName, timeTaken)

                  alreadyReportedToAnalytics.set(true)
                }
              }
              .map { it.value() }
        }
  }
}
