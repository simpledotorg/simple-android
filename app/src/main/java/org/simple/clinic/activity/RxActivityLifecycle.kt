package org.simple.clinic.activity

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import org.simple.clinic.activity.ActivityLifecycle.Destroyed
import org.simple.clinic.activity.ActivityLifecycle.Paused
import org.simple.clinic.activity.ActivityLifecycle.Resumed
import org.simple.clinic.activity.ActivityLifecycle.Started
import org.simple.clinic.activity.ActivityLifecycle.Stopped

class RxActivityLifecycle internal constructor(private val events: Observable<ActivityLifecycle>) {

  fun stream(): Observable<ActivityLifecycle> {
    return events
  }

  companion object {

    fun from(theActivity: AppCompatActivity): RxActivityLifecycle {

      val lifecycleEvents = Observable.create<ActivityLifecycle> { emitter ->
        val callbacks = RxActivityLifecycleCallbacks(theActivity, emitter)

        emitter.setCancellable { theActivity.application.unregisterActivityLifecycleCallbacks(callbacks) }
        theActivity.application.registerActivityLifecycleCallbacks(callbacks)
      }

      return RxActivityLifecycle(lifecycleEvents)
    }
  }
}

private class RxActivityLifecycleCallbacks(
    private val anActivity: AppCompatActivity,
    private val emitter: ObservableEmitter<ActivityLifecycle>
) : SimpleActivityLifecycleCallbacks() {

  private val activityName: String = anActivity.javaClass.simpleName

  override fun onActivityResumed(activity: Activity) {
    if (activity === anActivity) {
      emitter.onNext(Resumed(activityName))
    }
  }

  override fun onActivityStarted(activity: Activity) {
    if (activity === anActivity) {
      emitter.onNext(Started(activityName))
    }
  }

  override fun onActivityPaused(activity: Activity) {
    if (activity === anActivity) {
      emitter.onNext(Paused(activityName))
    }
  }

  override fun onActivityStopped(activity: Activity) {
    if (activity === anActivity) {
      emitter.onNext(Stopped(activityName))
    }
  }

  override fun onActivityDestroyed(activity: Activity) {
    if (activity === anActivity) {
      emitter.onNext(Destroyed(activityName))
      emitter.onComplete()
    }
  }
}
