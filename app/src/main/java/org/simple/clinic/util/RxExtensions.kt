package org.simple.clinic.util

import io.reactivex.Observable
import java.time.Duration
import java.util.concurrent.TimeUnit

fun <T> Observable<T>.debounce(duration: Duration): Observable<T> {
  return this
      .debounce(duration.toMillis(), TimeUnit.MILLISECONDS)
}
