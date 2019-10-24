package org.simple.clinic.await

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

private typealias Delay = Int
typealias Timing = Int

data class Checkpoint<T>(val item: T, val timing: Timing) {
  companion object {
    fun unit(timing: Timing): Checkpoint<Unit> =
        Checkpoint(Unit, timing)
  }
}

class Await<T>(
    private val checkpoints: List<Checkpoint<T>>,
    private val scheduler: Scheduler = Schedulers.computation()
) {
  fun events(): Observable<T> {
    val initialValue: Pair<Checkpoint<T?>, Delay> = Checkpoint(null as T?, 0) to 0
    val sortedCheckpoints = checkpoints.sortedBy { it.timing }

    return Observable
        .fromIterable(sortedCheckpoints)
        .scan(initialValue, { elapsedDelayPair, checkpoint ->
          val (previousCheckpoint, _) = elapsedDelayPair
          checkpoint as Checkpoint<T?> to checkpoint.timing - previousCheckpoint.timing
        })
        .skip(1)
        .concatMap { (checkpoint, delay) ->
          Observable
              .timer(delay.toLong(), TimeUnit.MILLISECONDS, scheduler)
              .map { checkpoint.item }
        }
  }
}
