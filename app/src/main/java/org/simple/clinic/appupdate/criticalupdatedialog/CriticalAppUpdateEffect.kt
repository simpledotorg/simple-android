package org.simple.clinic.appupdate.criticalupdatedialog

sealed class CriticalAppUpdateEffect

data object LoadAppUpdateHelpContact : CriticalAppUpdateEffect()

sealed class CriticalAppUpdateViewEffect : CriticalAppUpdateEffect()

data class OpenHelpContactUrl(val contactUrl: String) : CriticalAppUpdateViewEffect()

data object OpenSimpleInGooglePlay : CriticalAppUpdateViewEffect()

data object LoadAppStaleness : CriticalAppUpdateEffect()
