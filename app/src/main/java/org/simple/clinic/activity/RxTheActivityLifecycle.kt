package org.simple.clinic.activity

import android.app.Activity
import io.reactivex.Observable
import org.simple.clinic.activity.ActivityLifecycle.Destroyed
import org.simple.clinic.activity.ActivityLifecycle.Paused
import org.simple.clinic.activity.ActivityLifecycle.Resumed
import org.simple.clinic.activity.ActivityLifecycle.Started
import org.simple.clinic.activity.ActivityLifecycle.Stopped

class RxTheActivityLifecycle internal constructor(private val events: Observable<ActivityLifecycle>) {

  fun stream(): Observable<ActivityLifecycle> {
    return events
  }

  companion object {

    fun from(theActivity: TheActivity): RxTheActivityLifecycle {
      val lifecycleEvents = Observable.create<ActivityLifecycle> { emitter ->
        val callbacks = object : SimpleActivityLifecycleCallbacks() {
          override fun onActivityResumed(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Resumed("TheActivity"))
            }
          }

          override fun onActivityStarted(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Started("TheActivity"))
            }
          }

          override fun onActivityPaused(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Paused("TheActivity"))
            }
          }

          override fun onActivityStopped(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Stopped("TheActivity"))
            }
          }

          override fun onActivityDestroyed(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Destroyed("TheActivity"))
              emitter.onComplete()
            }
          }
        }

        emitter.setCancellable { theActivity.application.unregisterActivityLifecycleCallbacks(callbacks) }
        theActivity.application.registerActivityLifecycleCallbacks(callbacks)
      }

      return RxTheActivityLifecycle(lifecycleEvents)
    }
  }
}
