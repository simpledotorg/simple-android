package org.simple.clinic.introvideoscreen

import org.simple.clinic.widgets.UiEvent

sealed class IntroVideoEvent : UiEvent

object VideoClicked : IntroVideoEvent() {
  override val analyticsName: String = "IntroVideo:Video Clicked"
}

object SkipClicked : IntroVideoEvent() {
  override val analyticsName: String = "IntroVideo:Skip Clicked"
}
