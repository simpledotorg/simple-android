package org.resolvetosavelives.red.widgets

sealed class ActivityLifecycle : UiEvent {

  class Resumed : ActivityLifecycle()
  class Paused : ActivityLifecycle()
  class Destroyed : ActivityLifecycle()
}
