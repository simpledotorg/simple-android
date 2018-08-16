package org.simple.clinic.user

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "LoggedInUser")
data class LoggedInUser(

    @PrimaryKey
    val uuid: UUID,

    val fullName: String,

    val phoneNumber: String,

    val pinDigest: String,

    val status: UserStatus,

    val createdAt: Instant,

    val updatedAt: Instant
) {

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM LoggedInUser LIMIT 1")
    fun user(): Flowable<List<LoggedInUser>>

    @Query("SELECT * FROM LoggedInUser LIMIT 1")
    fun userImmediate(): LoggedInUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createOrUpdate(user: LoggedInUser)
  }

  fun isApprovedForSyncing(): Boolean {
    return status == UserStatus.APPROVED_FOR_SYNCING
  }
}
