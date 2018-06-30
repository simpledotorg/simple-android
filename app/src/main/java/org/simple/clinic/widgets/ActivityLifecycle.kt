package org.simple.clinic.widgets

sealed class ActivityLifecycle : UiEvent {

  class Resumed : ActivityLifecycle()
  class Started : ActivityLifecycle()
  class Paused : ActivityLifecycle()
  class Stopped : ActivityLifecycle()
  class Destroyed : ActivityLifecycle()
}
