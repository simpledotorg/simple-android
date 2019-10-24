package org.simple.clinic.await

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

private typealias Elapsed = Int
private typealias Delay = Int

class Await(
    private val delayMillis: List<Int>,
    private val scheduler: Scheduler = Schedulers.computation()
) {
  fun events(): Observable<Unit> {
    val initialElapsedDelay: Pair<Elapsed, Delay> = 0 to 0

    return Observable
        .fromIterable(delayMillis)
        .scan(initialElapsedDelay, { elapsedDelayPair, itemDelay ->
          val (elapsed, _) = elapsedDelayPair
          itemDelay to itemDelay - elapsed
        })
        .map { (_, delay) -> delay }
        .skip(1)
        .map { it.toLong() }
        .concatMap { delay -> Observable.timer(delay, TimeUnit.MILLISECONDS, scheduler).map { Unit } }
  }
}
