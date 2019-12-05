package org.simple.clinic.bloodsugar

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "BloodSugarMeasurements")
data class BloodSugarMeasurement(
    @PrimaryKey
    val uuid: UUID,

    @Embedded(prefix = "reading_")
    val reading: BloodSugarReading,

    val recordedAt: Instant,

    val patientUuid: UUID,

    val userUuid: UUID,

    val facilityUuid: UUID,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
)
