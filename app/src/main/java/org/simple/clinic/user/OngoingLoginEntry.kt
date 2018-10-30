package org.simple.clinic.user

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import java.util.UUID

@Entity(tableName = "OngoingLoginEntry")
data class OngoingLoginEntry(
    @PrimaryKey val uuid: UUID,
    val phoneNumber: String = "",
    val pin: String? = ""
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(loginEntry: OngoingLoginEntry)

    @Query("SELECT * FROM OngoingLoginEntry")
    fun getEntry(): Flowable<OngoingLoginEntry>

    @Query("DELETE FROM OngoingLoginEntry")
    fun delete()
  }
}
