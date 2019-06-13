package org.simple.clinic.user

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "OngoingLoginEntry")
data class OngoingLoginEntry(
    @PrimaryKey val uuid: UUID,

    val phoneNumber: String? = null,

    val pin: String? = null,

    val fullName: String? = null,

    val pinDigest: String? = null,

    val registrationFacilityUuid: UUID? = null,

    val status: UserStatus? = null,

    val createdAt: Instant? = null,

    val updatedAt: Instant? = null
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(loginEntry: OngoingLoginEntry)

    @Query("SELECT * FROM OngoingLoginEntry")
    fun getEntry(): Flowable<List<OngoingLoginEntry>>

    @Query("DELETE FROM OngoingLoginEntry")
    fun delete()
  }
}
