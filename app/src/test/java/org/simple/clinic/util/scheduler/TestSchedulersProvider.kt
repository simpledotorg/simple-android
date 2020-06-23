package org.simple.clinic.util.scheduler

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class TestSchedulersProvider(
    val ioScheduler: Scheduler,
    val computationScheduler: Scheduler,
    val uiScheduler: Scheduler
) : SchedulersProvider {

  companion object {
    fun trampoline(
        ioScheduler: Scheduler = Schedulers.trampoline(),
        computationScheduler: Scheduler = Schedulers.trampoline(),
        uiScheduler: Scheduler = Schedulers.trampoline()
    ): SchedulersProvider {
      return TestSchedulersProvider(ioScheduler, computationScheduler, uiScheduler)
    }
  }

  override fun io(): Scheduler = ioScheduler

  override fun computation(): Scheduler = computationScheduler

  override fun ui(): Scheduler = uiScheduler
}
