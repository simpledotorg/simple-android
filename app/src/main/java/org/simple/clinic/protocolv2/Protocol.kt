package org.simple.clinic.protocolv2

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import java.util.UUID

@Entity
data class Protocol(

    @PrimaryKey
    val uuid: UUID,

    val name: String,

    val followUpDays: Int,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncStatus: SyncStatus,

    val deletedAt: Instant?
)
