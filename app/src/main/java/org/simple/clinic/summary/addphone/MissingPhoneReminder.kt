package org.simple.clinic.summary.addphone

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import org.simple.clinic.patient.PatientUuid
import java.time.Instant

@Entity(tableName = "MissingPhoneReminder")
data class MissingPhoneReminder(
    @PrimaryKey
    val patientUuid: PatientUuid,
    val remindedAt: Instant
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(reminder: MissingPhoneReminder)

    @Query("SELECT * FROM MissingPhoneReminder WHERE patientUuid = :patientUuid")
    fun get(patientUuid: PatientUuid): Flowable<List<MissingPhoneReminder>>

    @Query("SELECT * FROM MissingPhoneReminder WHERE patientUuid = :patientUuid")
    fun forPatient(patientUuid: PatientUuid): MissingPhoneReminder?
  }
}
