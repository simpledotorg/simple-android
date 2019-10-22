package org.simple.clinic.encounter

import androidx.room.Embedded
import androidx.room.Relation
import org.simple.clinic.bp.BloodPressureMeasurement

data class ObservationsForEncounter(
    @Embedded
    val encounter: Encounter,

    @Relation(parentColumn = "uuid", entityColumn = "encounterUuid")
    val bloodPressures: List<BloodPressureMeasurement>
)
