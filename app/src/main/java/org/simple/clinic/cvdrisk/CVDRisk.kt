package org.simple.clinic.cvdrisk

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
    tableName = "CVDRisk",
    indices = [
      Index("patientUuid", unique = false)
    ]
)
data class CVDRisk(
    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val riskScore: String,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
) : Parcelable
