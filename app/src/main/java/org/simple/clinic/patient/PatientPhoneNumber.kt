package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import kotlinx.parcelize.Parcelize
import java.time.Instant
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
@Parcelize
data class PatientPhoneNumber(
    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val number: String,

    val phoneType: PatientPhoneNumberType,

    val active: Boolean,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) : Parcelable {

  fun withNumber(number: String): PatientPhoneNumber =
      copy(number = number)

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(phoneNumbers: List<PatientPhoneNumber>)

    @Query("SELECT * FROM patientphonenumber WHERE patientUuid = :patientUuid")
    fun phoneNumber(patientUuid: UUID): Flowable<List<PatientPhoneNumber>>

    @Query("""
      SELECT * FROM PatientPhoneNumber 
      WHERE patientUuid = :patientUuid AND deletedAt IS NULL 
      ORDER BY createdAt DESC LIMIT 1
    """)
    fun latestPhoneNumber(patientUuid: UUID): PatientPhoneNumber?

    @Query("DELETE FROM patientphonenumber")
    fun clear()

    @Query("SELECT COUNT(uuid) FROM PatientPhoneNumber")
    fun count(): Int
  }
}
