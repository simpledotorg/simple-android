package org.simple.clinic.activity

import org.simple.clinic.widgets.UiEvent

sealed class TheActivityLifecycle : UiEvent {

  class Resumed : TheActivityLifecycle() {
    override val analyticsName = "TheActivity:Resumed"
  }

  class Started : TheActivityLifecycle() {
    override val analyticsName = "TheActivity:Started"
  }

  class Paused : TheActivityLifecycle() {
    override val analyticsName = "TheActivity:Paused"
  }

  class Stopped : TheActivityLifecycle() {
    override val analyticsName = "TheActivity:Stopped"
  }

  class Destroyed : TheActivityLifecycle() {
    override val analyticsName = "TheActivity:Destroyed"
  }
}
