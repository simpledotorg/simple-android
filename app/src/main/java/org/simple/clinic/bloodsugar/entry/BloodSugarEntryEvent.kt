package org.simple.clinic.bloodsugar.entry

sealed class BloodSugarEntryEvent

data class BloodSugarChanged(
    val bloodSugarReading: String
) : BloodSugarEntryEvent()

data class DayChanged(
    val day: String
) : BloodSugarEntryEvent()

data class MonthChanged(
    val month: String
) : BloodSugarEntryEvent()

data class YearChanged(
    val twoDigitYear: String
) : BloodSugarEntryEvent()

object BackPressed : BloodSugarEntryEvent()

object BloodSugarDateClicked : BloodSugarEntryEvent()

object ShowBloodSugarEntryClicked : BloodSugarEntryEvent()

