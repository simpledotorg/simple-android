package org.simple.clinic.util

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.allpatientsinfacility.SchedulersProvider

class DefaultSchedulersProvider : SchedulersProvider {
  override fun io(): Scheduler = Schedulers.io()

  override fun ui(): Scheduler = AndroidSchedulers.mainThread()

  override fun computation(): Scheduler = Schedulers.computation()
}
