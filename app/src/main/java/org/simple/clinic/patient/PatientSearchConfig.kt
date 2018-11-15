package org.simple.clinic.patient

import org.simple.clinic.patient.fuzzy.AgeFuzzer

data class PatientSearchConfig(
    val ageFuzzer: AgeFuzzer,
    val fuzzyStringDistanceCutoff: Float,
    val characterSubstitutionCost: Float,
    val characterInsertionCost: Float,
    val characterDeletionCost: Float
)
