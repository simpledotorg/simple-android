package org.simple.clinic.user

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
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
    fun getEntry(): Flowable<List<OngoingLoginEntry>>

    @Query("DELETE FROM OngoingLoginEntry")
    fun delete()
  }
}
