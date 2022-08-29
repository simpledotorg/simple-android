package org.simple.clinic.introvideoscreen

sealed class IntroVideoEffect

sealed class IntroVideoViewEffect : IntroVideoEffect()

object OpenVideo : IntroVideoViewEffect()

object OpenHome : IntroVideoViewEffect()
