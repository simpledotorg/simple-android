package org.simple.clinic.activity

import org.simple.clinic.widgets.UiEvent

private fun event(activityName: String?, event: String): String = activityName?.let { "$activityName:$event" } ?: ""

sealed class TheActivityLifecycle : UiEvent {

  class Resumed(activityName: String?) : TheActivityLifecycle() {
    override val analyticsName = event(activityName, "Resumed")
  }

  class Started(activityName: String?) : TheActivityLifecycle() {
    override val analyticsName = event(activityName, "Started")
  }

  class Paused(activityName: String?) : TheActivityLifecycle() {
    override val analyticsName = event(activityName, "Paused")
  }

  class Stopped(activityName: String?) : TheActivityLifecycle() {
    override val analyticsName = event(activityName, "Stopped")
  }

  class Destroyed(activityName: String?) : TheActivityLifecycle() {
    override val analyticsName = event(activityName, "Destroyed")
  }
}
