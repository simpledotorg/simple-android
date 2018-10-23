package org.simple.clinic.summary

import org.threeten.bp.Duration

data class PatientSummaryConfig(val numberOfBpPlaceholders: Int, val bpEditableFor: Duration)
