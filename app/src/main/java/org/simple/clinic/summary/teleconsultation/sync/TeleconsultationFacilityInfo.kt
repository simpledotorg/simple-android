package org.simple.clinic.summary.teleconsultation.sync

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import java.time.Instant
import java.util.UUID

@Entity
data class TeleconsultationFacilityInfo(
    @PrimaryKey
    val teleconsultationFacilityId: UUID,

    val facilityId: UUID,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = REPLACE)
    fun save(teleconsultationFacilityInfo: List<TeleconsultationFacilityInfo>)

    @Query("DELETE FROM TeleconsultationFacilityInfo")
    fun clear()
  }
}
