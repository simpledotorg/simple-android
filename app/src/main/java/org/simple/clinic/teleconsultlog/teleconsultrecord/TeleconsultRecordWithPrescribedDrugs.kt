package org.simple.clinic.teleconsultlog.teleconsultrecord

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation

data class TeleconsultRecordWithPrescribedDrugs(
    @Embedded
    val teleconsultRecord: TeleconsultRecord,

    @Relation(
        parentColumn = "id",
        entity = TeleconsultRecordPrescribedDrug::class,
        entityColumn = "teleconsultRecordId"
    )
    val prescribedDrugs: List<TeleconsultRecordPrescribedDrug>

) {
  @Dao
  interface RoomDao {

    @Query("SELECT * FROM TeleconsultRecord")
    fun getPrescribedUuidForTeleconsultRecordUuid(): List<TeleconsultRecordWithPrescribedDrugs>
  }
}
