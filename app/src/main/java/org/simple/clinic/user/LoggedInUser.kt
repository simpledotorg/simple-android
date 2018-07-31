package org.simple.clinic.user

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.reactivex.Flowable
import org.simple.clinic.util.RoomEnumTypeConverter
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "LoggedInUser")
@JsonClass(generateAdapter = true)
data class LoggedInUser(

    @PrimaryKey
    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "phone_number")
    val phoneNumber: String,

    @Json(name = "password_digest")
    val pinDigest: String,

    @Json(name = "facility_id")
    val facilityUuid: UUID,

    @Json(name = "status")
    val status: Status,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant
) {

  enum class Status {
    @Json(name = "waiting_for_approval")
    WAITING_FOR_APPROVAL;

    class RoomTypeConverter : RoomEnumTypeConverter<Status>(Status::class.java)
  }

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM LoggedInUser")
    fun user(): Flowable<List<LoggedInUser>>

    @Insert
    fun create(user: LoggedInUser)
  }
}
