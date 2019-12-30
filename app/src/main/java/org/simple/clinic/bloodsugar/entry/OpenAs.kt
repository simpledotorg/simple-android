package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import java.util.UUID

sealed class OpenAs

data class New(val patientId: UUID, val measurementType: BloodSugarMeasurementType) : OpenAs()
