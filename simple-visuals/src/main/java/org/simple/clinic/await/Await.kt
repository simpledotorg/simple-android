package org.simple.clinic.await

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class Await(
    private val delayMillis: List<Int>,
    private val scheduler: Scheduler = Schedulers.computation()
) {
  fun events(): Observable<Unit> {
    return Observable
        .fromIterable(delayMillis)
        .scan(0, { delaySoFar, delay -> delay - delaySoFar })
        .skip(1)
        .concatMap { Observable.timer(it.toLong(), TimeUnit.MILLISECONDS, scheduler).map { Unit } }
  }
}
