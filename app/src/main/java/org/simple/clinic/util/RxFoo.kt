package org.simple.clinic.util

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Timed
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

inline fun <reified U> Single<*>.ofType(): Maybe<U> =
    filter { it is U }
        .cast(U::class.java)

fun Observables.timer(duration: Duration): Observable<Long> = Observable.timer(duration.toMillis(), TimeUnit.MILLISECONDS)

operator fun Timed<*>.minus(timed: Timed<*>): Duration {
  val durationMillis = time(TimeUnit.MILLISECONDS) - timed.time(TimeUnit.MILLISECONDS)

  return Duration.ofMillis(durationMillis)
}

fun Observable<Boolean>.filterTrue(): Observable<Boolean> {
  return this.filter { isTrue -> isTrue }
}

inline fun <T : Any, Y : Any> Observable<T>.extractNullable(crossinline mapper: (T) -> Y?): Observable<Y> {
  return this
      .map { mapper(it).toOptional() }
      .filterAndUnwrapJust()
}
