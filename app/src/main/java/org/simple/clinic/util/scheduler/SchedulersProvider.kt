package org.simple.clinic.util.scheduler

import io.reactivex.Scheduler

interface SchedulersProvider {
  fun io(): Scheduler
  fun computation(): Scheduler
  fun ui(): Scheduler
}
