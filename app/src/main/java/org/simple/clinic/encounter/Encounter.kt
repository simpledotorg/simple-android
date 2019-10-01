package org.simple.clinic.encounter

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@Entity
data class Encounter(

    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val encounteredOn: LocalDate,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
)
