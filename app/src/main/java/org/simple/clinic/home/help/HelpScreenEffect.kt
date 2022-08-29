package org.simple.clinic.home.help

sealed class HelpScreenEffect

object LoadHelpContent : HelpScreenEffect()

object SyncHelp : HelpScreenEffect()

sealed class HelpScreenViewEffect : HelpScreenEffect()

object ShowLoadingView : HelpScreenViewEffect()
