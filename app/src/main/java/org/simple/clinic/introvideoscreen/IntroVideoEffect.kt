package org.simple.clinic.introvideoscreen

sealed class IntroVideoEffect

object OpenHome : IntroVideoEffect()

sealed class IntroVideoViewEffect : IntroVideoEffect()

object OpenVideo : IntroVideoViewEffect()
