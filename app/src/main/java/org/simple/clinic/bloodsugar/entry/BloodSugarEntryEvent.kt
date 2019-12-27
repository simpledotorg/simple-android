package org.simple.clinic.bloodsugar.entry

sealed class BloodSugarEntryEvent

object BloodSugarChanged : BloodSugarEntryEvent()
