package org.simple.clinic.activity

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
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

    fun from(theActivity: AppCompatActivity): RxTheActivityLifecycle {
      val activityName = theActivity.javaClass.simpleName

      val lifecycleEvents = Observable.create<ActivityLifecycle> { emitter ->
        val callbacks = object : SimpleActivityLifecycleCallbacks() {
          override fun onActivityResumed(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Resumed(activityName))
            }
          }

          override fun onActivityStarted(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Started(activityName))
            }
          }

          override fun onActivityPaused(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Paused(activityName))
            }
          }

          override fun onActivityStopped(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Stopped(activityName))
            }
          }

          override fun onActivityDestroyed(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(Destroyed(activityName))
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
