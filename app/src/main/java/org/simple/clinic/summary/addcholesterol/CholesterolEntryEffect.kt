package org.simple.clinic.summary.addcholesterol

import java.util.UUID

sealed interface CholesterolEntryEffect

data object HideCholesterolErrorMessage : CholesterolEntryEffect

data object ShowReqMinCholesterolValidationError : CholesterolEntryEffect

data object ShowReqMaxCholesterolValidationError : CholesterolEntryEffect

data class SaveCholesterol(val patientUuid: UUID, val cholesterolValue: Float) : CholesterolEntryEffect
