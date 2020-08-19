package org.simple.clinic.user

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import kotlinx.android.parcel.Parcelize
import java.time.Instant
import java.util.UUID

@Entity(tableName = "OngoingLoginEntry")
@Parcelize
data class OngoingLoginEntry(
    @PrimaryKey val uuid: UUID,

    val phoneNumber: String? = null,

    val pin: String? = null,

    val fullName: String? = null,

    val pinDigest: String? = null,

    val registrationFacilityUuid: UUID? = null,

    val status: UserStatus? = null,

    val createdAt: Instant? = null,

    val updatedAt: Instant? = null,

    val teleconsultPhoneNumber: String? = null
) : Parcelable {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(loginEntry: OngoingLoginEntry)

    @Query("SELECT * FROM OngoingLoginEntry")
    fun getEntry(): Flowable<List<OngoingLoginEntry>>

    @Query("SELECT * FROM OngoingLoginEntry")
    fun getEntryImmediate(): OngoingLoginEntry?

    @Query("DELETE FROM OngoingLoginEntry")
    fun delete()
  }
}
