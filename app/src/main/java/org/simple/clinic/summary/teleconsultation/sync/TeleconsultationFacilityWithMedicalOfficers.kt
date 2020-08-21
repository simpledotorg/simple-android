package org.simple.clinic.summary.teleconsultation.sync

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import java.util.UUID

@Entity(
    primaryKeys = ["teleconsultationFacilityId", "medicalOfficerId"]
)
data class TeleconsultationFacilityMedicalOfficersCrossRef(
    val teleconsultationFacilityId: UUID,
    val medicalOfficerId: UUID
)

data class TeleconsultationFacilityWithMedicalOfficers(

    @Embedded
    val teleconsultationFacilityInfo: TeleconsultationFacilityInfo,

    @Relation(
        parentColumn = "teleconsultationFacilityId",
        entity = MedicalOfficer::class,
        entityColumn = "medicalOfficerId",
        associateBy = Junction(TeleconsultationFacilityMedicalOfficersCrossRef::class)
    )
    val medicalOfficers: List<MedicalOfficer>
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(teleconsultationFacilitiesWithMedicalOfficerCrossRefs: List<TeleconsultationFacilityMedicalOfficersCrossRef>)

    @Transaction
    @Query("SELECT * FROM TeleconsultationFacilityInfo WHERE facilityId = :facilityId AND deletedAt IS NULL")
    fun getOne(facilityId: UUID): TeleconsultationFacilityWithMedicalOfficers?

    @Transaction
    @Query("SELECT * FROM TeleconsultationFacilityInfo WHERE deletedAt IS NULL")
    fun getAll(): List<TeleconsultationFacilityWithMedicalOfficers>

    @Query("DELETE FROM TeleconsultationFacilityMedicalOfficersCrossRef")
    fun clear()
  }
}
