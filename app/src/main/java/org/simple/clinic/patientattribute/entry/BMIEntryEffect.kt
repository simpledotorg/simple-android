package org.simple.clinic.patientattribute.entry

import org.simple.clinic.patientattribute.BMIReading
import java.util.UUID

sealed class BMIEntryEffect

data class CreateNewBMIEntry(
    val patientUUID: UUID,
    val reading: BMIReading
) : BMIEntryEffect()
