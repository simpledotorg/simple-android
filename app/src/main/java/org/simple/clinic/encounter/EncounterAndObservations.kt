package org.simple.clinic.encounter

import org.simple.clinic.bp.BloodPressureMeasurement

data class EncounterAndObservations (

    val encounter: Encounter,

    val bloodPressures: List<BloodPressureMeasurement>
)
