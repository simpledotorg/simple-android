package org.simple.clinic.introvideoscreen

import org.simple.clinic.widgets.UiEvent

sealed class IntroVideoEvent : UiEvent

object VideoClicked : IntroVideoEvent()

object SkipClicked : IntroVideoEvent()
