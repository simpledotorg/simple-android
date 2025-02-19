package org.simple.clinic.summary.addcholesterol

sealed interface CholesterolEntryEvent

data class CholesterolChanged(val cholesterolValue: Float) : CholesterolEntryEvent

data object SaveClicked : CholesterolEntryEvent

data object CholesterolSaved : CholesterolEntryEvent

data object KeyboardClosed : CholesterolEntryEvent
