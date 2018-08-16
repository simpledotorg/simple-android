package org.simple.clinic.widgets

sealed class TheActivityLifecycle : UiEvent {
  class Resumed : TheActivityLifecycle()
  class Started : TheActivityLifecycle()
  class Paused : TheActivityLifecycle()
  class Stopped : TheActivityLifecycle()
  class Destroyed : TheActivityLifecycle()
}
