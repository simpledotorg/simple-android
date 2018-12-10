package org.simple.clinic.protocolv2

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import java.util.UUID

@Entity(
    foreignKeys = [
      ForeignKey(entity = Protocol::class,
          parentColumns = ["uuid"],
          childColumns = ["protocolUuid"],
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.NO_ACTION
      )],
    indices = [
      Index("protocolUuid",
          unique = false
      )]
)
data class ProtocolDrug(

    @PrimaryKey
    val uuid: UUID,

    val protocolUuid: UUID,

    val name: String,

    val rxNormCode: String?,

    val dosage: String,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncStatus: SyncStatus
)
