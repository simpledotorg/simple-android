package org.simple.clinic.encounter

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.PrimaryKey
import org.simple.clinic.patient.SyncStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@Entity
data class Encounter(

    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val encounteredOn: LocalDate,

    val syncStatus: SyncStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = REPLACE)
    fun save(encounters: List<Encounter>)
  }
}
