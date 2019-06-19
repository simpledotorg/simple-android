package org.simple.clinic.allpatientsinfacility

import io.reactivex.Scheduler

interface SchedulersProvider {
  fun io(): Scheduler
  fun computation(): Scheduler
  fun ui(): Scheduler
}
