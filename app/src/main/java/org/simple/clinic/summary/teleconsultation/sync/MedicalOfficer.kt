package org.simple.clinic.summary.teleconsultation.sync

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import java.util.UUID

@Entity
data class MedicalOfficer(
    @PrimaryKey
    val medicalOfficerId: UUID,

    val fullName: String,

    val phoneNumber: String
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(medicalOfficers: List<MedicalOfficer>)

    @Query("SELECT * FROM MedicalOfficer")
    fun getAll(): List<MedicalOfficer>

    @Query("DELETE FROM MedicalOfficer")
    fun clear()
  }
}
