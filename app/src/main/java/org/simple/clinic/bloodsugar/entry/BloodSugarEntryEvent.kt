package org.simple.clinic.bloodsugar.entry

sealed class BloodSugarEntryEvent

data class BloodSugarChanged(
    val bloodSugarReading: String
) : BloodSugarEntryEvent()

object DayChanged : BloodSugarEntryEvent()

object MonthChanged : BloodSugarEntryEvent()

object YearChanged : BloodSugarEntryEvent()

object BackPressed : BloodSugarEntryEvent()

object BloodSugarDateClicked : BloodSugarEntryEvent()
