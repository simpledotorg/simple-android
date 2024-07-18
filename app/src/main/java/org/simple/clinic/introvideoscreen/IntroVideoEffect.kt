package org.simple.clinic.introvideoscreen

sealed class IntroVideoEffect

sealed class IntroVideoViewEffect : IntroVideoEffect()

data object OpenVideo : IntroVideoViewEffect()

data object OpenHome : IntroVideoViewEffect()
