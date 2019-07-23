package org.simple.clinic.util.scheduler

import io.reactivex.Scheduler
import io.reactivex.schedulers.TestScheduler

class TestSchedulersProvider: SchedulersProvider {

  val testScheduler: TestScheduler = TestScheduler()

  override fun io(): Scheduler = testScheduler

  override fun computation(): Scheduler = testScheduler

  override fun ui(): Scheduler = testScheduler
}
