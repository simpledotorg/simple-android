package org.resolvetosavelives.red.widgets

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import io.reactivex.Observable
import org.resolvetosavelives.red.TheActivity

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
              emitter.onNext(ActivityLifecycle.Resumed())
            }
          }

          override fun onActivityPaused(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(ActivityLifecycle.Paused())
            }
          }

          override fun onActivityDestroyed(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(ActivityLifecycle.Destroyed())
              emitter.onComplete()
            }
          }
        }

        theActivity.application.registerActivityLifecycleCallbacks(callbacks)
        emitter.setCancellable { theActivity.application.unregisterActivityLifecycleCallbacks(callbacks) }
      }

      return RxTheActivityLifecycle(lifecycleEvents)
    }
  }
}

private abstract class SimpleActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {}

  override fun onActivityStarted(activity: Activity) {}

  override fun onActivityResumed(activity: Activity) {}

  override fun onActivityPaused(activity: Activity) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

  override fun onActivityDestroyed(activity: Activity) {}
}
