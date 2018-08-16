package org.simple.clinic.widgets

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import io.reactivex.Observable
import org.simple.clinic.activity.TheActivity

class RxTheActivityLifecycle internal constructor(private val events: Observable<TheActivityLifecycle>) {

  fun stream(): Observable<TheActivityLifecycle> {
    return events
  }

  companion object {

    fun from(theActivity: TheActivity): RxTheActivityLifecycle {
      val lifecycleEvents = Observable.create<TheActivityLifecycle> { emitter ->
        val callbacks = object : SimpleActivityLifecycleCallbacks() {
          override fun onActivityResumed(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(TheActivityLifecycle.Resumed())
            }
          }

          override fun onActivityStarted(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(TheActivityLifecycle.Started())
            }
          }

          override fun onActivityPaused(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(TheActivityLifecycle.Paused())
            }
          }

          override fun onActivityStopped(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(TheActivityLifecycle.Stopped())
            }
          }

          override fun onActivityDestroyed(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(TheActivityLifecycle.Destroyed())
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

abstract class SimpleActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

  override fun onActivityStarted(activity: Activity) {}

  override fun onActivityResumed(activity: Activity) {}

  override fun onActivityPaused(activity: Activity) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

  override fun onActivityDestroyed(activity: Activity) {}
}
