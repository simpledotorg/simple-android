package org.simple.clinic.home.help

sealed class HelpScreenEffect

data object LoadHelpContent : HelpScreenEffect()

data object SyncHelp : HelpScreenEffect()

sealed class HelpScreenViewEffect : HelpScreenEffect()

data object ShowLoadingView : HelpScreenViewEffect()
