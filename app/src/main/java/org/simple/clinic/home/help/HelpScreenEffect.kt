package org.simple.clinic.home.help

sealed class HelpScreenEffect

object ShowLoadingView : HelpScreenEffect()

object LoadHelpContent : HelpScreenEffect()

object SyncHelp : HelpScreenEffect()
