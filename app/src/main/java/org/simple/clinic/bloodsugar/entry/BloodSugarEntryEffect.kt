package org.simple.clinic.bloodsugar.entry

sealed class BloodSugarEntryEffect

object HideBloodSugarErrorMessage : BloodSugarEntryEffect()

object HideDateErrorMessage : BloodSugarEntryEffect()

object Dismiss : BloodSugarEntryEffect()
