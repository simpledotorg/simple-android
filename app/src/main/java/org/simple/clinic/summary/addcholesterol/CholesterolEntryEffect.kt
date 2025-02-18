package org.simple.clinic.summary.addcholesterol

sealed interface CholesterolEntryEffect

data object HideCholesterolErrorMessage : CholesterolEntryEffect

data object ShowReqMinCholesterolValidationError : CholesterolEntryEffect
