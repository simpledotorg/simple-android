package org.resolvetosavelives.red.search

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
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
    fun save(phoneNumber: PatientPhoneNumber)
  }
}
