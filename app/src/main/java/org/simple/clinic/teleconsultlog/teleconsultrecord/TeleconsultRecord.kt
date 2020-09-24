package org.simple.clinic.teleconsultlog.teleconsultrecord

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import org.simple.clinic.storage.Timestamps
import java.time.Instant
import java.util.UUID

@Entity
data class TeleconsultRecord(
    @PrimaryKey
    val id: UUID,

    val patientId: UUID,

    val medicalOfficerId: UUID,

    @Embedded(prefix = "request_")
    val teleconsultRequestInfo: TeleconsultRequestInfo?,

    @Embedded(prefix = "record_")
    val teleconsultRecordInfo: TeleconsultRecordInfo?,

    @Embedded
    val timestamp: Timestamps
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = REPLACE)
    fun save(teleconsultRecords: List<TeleconsultRecord>)

    @Query("DELETE FROM TeleconsultRecord")
    fun clear()

    @Query("SELECT * FROM TeleconsultRecord")
    fun getAll(): List<TeleconsultRecord>

    @Query("SELECT * FROM TeleconsultRecord WHERE id = :teleconsultRecordId")
    fun getCompleteTeleconsultLog(teleconsultRecordId: UUID) : TeleconsultRecord

    @Query("UPDATE TeleconsultRecord SET record_medicalOfficerNumber = :medicalOfficerNumber, updatedAt = :updatedAt WHERE id = :teleconsultRecordId")
    fun updateMedicalRegistrationId(teleconsultRecordId: UUID, medicalOfficerNumber: String, updatedAt: Instant)
  }
}
