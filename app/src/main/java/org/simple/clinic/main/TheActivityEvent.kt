package org.simple.clinic.main

import org.simple.clinic.widgets.UiEvent

sealed class TheActivityEvent : UiEvent

sealed class LifecycleEvent : TheActivityEvent() {
  object ActivityStarted : LifecycleEvent()
  object ActivityStopped : LifecycleEvent()
  object ActivityDestroyed : LifecycleEvent()
}


