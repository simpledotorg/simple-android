package org.simple.clinic.summary.teleconsultation.sync

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.Unicode
import java.util.UUID

@Entity
@Parcelize
data class MedicalOfficer(
    @PrimaryKey
    val medicalOfficerId: UUID,

    val fullName: String,

    val phoneNumber: String
) : Parcelable {

  override fun toString(): String {
    return "MedicalOfficer(${Unicode.redacted})"
  }

  fun toPayload(): MedicalOfficerPayload {
    return MedicalOfficerPayload(
        id = medicalOfficerId,
        fullName = fullName,
        phoneNumber = phoneNumber
    )
  }

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
