package org.simple.clinic.util

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

inline fun <reified U> Single<*>.ofType(): Maybe<U> =
    filter { it is U }
        .cast(U::class.java)

fun Observables.timer(duration: Duration): Observable<Long> = Observable.timer(duration.toMillis(), TimeUnit.MILLISECONDS)
