package org.simple.clinic.activity

import org.simple.clinic.widgets.UiEvent

private fun event(
    activityName: String?,
    event: String
): String = activityName?.let { "$activityName:$event" } ?: ""

sealed class ActivityLifecycle : UiEvent {

  class Started(activityName: String?) : ActivityLifecycle() {
    override val analyticsName = event(activityName, "Started")
  }

  class Resumed(activityName: String?) : ActivityLifecycle() {
    override val analyticsName = event(activityName, "Resumed")
  }

  class Paused(activityName: String?) : ActivityLifecycle() {
    override val analyticsName = event(activityName, "Paused")
  }

  class Stopped(activityName: String?) : ActivityLifecycle() {
    override val analyticsName = event(activityName, "Stopped")
  }

  class Destroyed(activityName: String?) : ActivityLifecycle() {
    override val analyticsName = event(activityName, "Destroyed")
  }
}
