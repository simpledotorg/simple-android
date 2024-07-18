package org.simple.clinic.introvideoscreen

import org.simple.clinic.widgets.UiEvent

sealed class IntroVideoEvent : UiEvent

data object VideoClicked : IntroVideoEvent() {
  override val analyticsName: String = "IntroVideo:Video Clicked"
}

data object SkipClicked : IntroVideoEvent() {
  override val analyticsName: String = "IntroVideo:Skip Clicked"
}
