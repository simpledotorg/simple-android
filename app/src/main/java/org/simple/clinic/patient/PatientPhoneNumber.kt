package org.simple.clinic.patient

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.threeten.bp.Instant
import java.util.UUID

@Entity(
    foreignKeys = [
      ForeignKey(
          entity = Patient::class,
          parentColumns = ["uuid"],
          childColumns = ["patientUuid"],
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE)
    ],
    indices = [
      (Index("patientUuid", unique = false))
    ])
data class PatientPhoneNumber(
    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val number: String,

    val phoneType: PatientPhoneNumberType,

    val active: Boolean,

    val createdAt: Instant,

    val updatedAt: Instant
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(phoneNumbers: List<PatientPhoneNumber>)

    @Query("SELECT * FROM patientphonenumber WHERE patientUuid = :patientUuid")
    fun phoneNumber(patientUuid: UUID): Flowable<List<PatientPhoneNumber>>

    @Query("DELETE FROM patientphonenumber")
    fun clear()

    @Query("SELECT COUNT(uuid) FROM PatientPhoneNumber")
    fun count(): Int
  }
}
