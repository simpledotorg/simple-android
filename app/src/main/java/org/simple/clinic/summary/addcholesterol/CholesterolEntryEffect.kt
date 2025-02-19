package org.simple.clinic.summary.addcholesterol

import java.util.UUID

sealed interface CholesterolEntryEffect

data class SaveCholesterol(val patientUuid: UUID, val cholesterolValue: Float) : CholesterolEntryEffect

sealed interface CholesterolEntryViewEffect : CholesterolEntryEffect

data object HideCholesterolErrorMessage : CholesterolEntryViewEffect

data object ShowReqMinCholesterolValidationError : CholesterolEntryViewEffect

data object ShowReqMaxCholesterolValidationError : CholesterolEntryViewEffect
