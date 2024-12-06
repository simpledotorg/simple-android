package org.simple.clinic.patientattribute

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import java.util.UUID

@Parcelize
@Entity(
    tableName = "PatientAttribute",
    indices = [
      Index("patientUuid", unique = false)
    ]
)
data class PatientAttribute(
    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val userUuid: UUID,

    @Embedded
    val reading: BMIReading,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
) : Parcelable
