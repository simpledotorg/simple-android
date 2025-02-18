package org.simple.clinic.summary.addcholesterol

sealed interface CholesterolEntryEvent

data class CholesterolChanged(val cholesterolValue: Float) : CholesterolEntryEvent
