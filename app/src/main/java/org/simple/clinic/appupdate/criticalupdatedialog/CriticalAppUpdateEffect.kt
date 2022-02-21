package org.simple.clinic.appupdate.criticalupdatedialog

sealed class CriticalAppUpdateEffect

object LoadAppUpdateHelpContact : CriticalAppUpdateEffect()

sealed class CriticalAppUpdateViewEffect : CriticalAppUpdateEffect()

data class OpenHelpContactUrl(val contactUrl: String) : CriticalAppUpdateViewEffect()

object OpenSimpleInGooglePlay : CriticalAppUpdateViewEffect()
