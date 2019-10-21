package org.simple.clinic.activity

import android.app.Activity
import io.reactivex.Observable

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
              emitter.onNext(TheActivityLifecycle.Resumed("TheActivity"))
            }
          }

          override fun onActivityStarted(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(TheActivityLifecycle.Started("TheActivity"))
            }
          }

          override fun onActivityPaused(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(TheActivityLifecycle.Paused("TheActivity"))
            }
          }

          override fun onActivityStopped(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(TheActivityLifecycle.Stopped("TheActivity"))
            }
          }

          override fun onActivityDestroyed(activity: Activity) {
            if (activity === theActivity) {
              emitter.onNext(TheActivityLifecycle.Destroyed("TheActivity"))
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
