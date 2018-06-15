package org.simple.clinic.widgets

sealed class ActivityLifecycle : UiEvent {

  class Resumed : ActivityLifecycle()
  class Paused : ActivityLifecycle()
  class Destroyed : ActivityLifecycle()
}
