package org.simple.clinic.await

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MILLISECONDS

private typealias Delay = Int
typealias TimingInMillis = Int

data class Checkpoint<T>(val item: T, val timingInMillis: TimingInMillis) {
  companion object {
    fun unit(timingInMillis: TimingInMillis): Checkpoint<Unit> =
        Checkpoint(Unit, timingInMillis)
  }
}

class Await<T>(
    private val checkpoints: List<Checkpoint<T>>,
    private val scheduler: Scheduler = Schedulers.computation()
) {
  fun items(): Observable<T> {
    val initialValue: Pair<Checkpoint<T?>, Delay> = Checkpoint(null as T?, 0) to 0
    val sortedCheckpoints = checkpoints.sortedBy { it.timingInMillis }
    assertNoDuplicateTimings(sortedCheckpoints)

    return Observable
        .fromIterable(sortedCheckpoints)
        .scan(initialValue, { elapsedDelayPair, checkpoint ->
          val (previousCheckpoint, _) = elapsedDelayPair
          checkpoint as Checkpoint<T?> to checkpoint.timingInMillis - previousCheckpoint.timingInMillis
        })
        .skip(1)
        .concatMap { (checkpoint, delay) ->
          Observable
              .timer(delay.toLong(), MILLISECONDS, scheduler)
              .map { checkpoint.item }
        }
  }

  private fun assertNoDuplicateTimings(checkpoints: List<Checkpoint<T>>) {
    checkpoints
        .map { it.timingInMillis }
        .foldRightIndexed(0) { index, timing, previousTiming ->
          if (timing == previousTiming && index != 0) {
            throw IllegalArgumentException("Found duplicate timing '$timing'. Duplicate timing values are not allowed.")
          } else {
            timing
          }
        }
  }
}
