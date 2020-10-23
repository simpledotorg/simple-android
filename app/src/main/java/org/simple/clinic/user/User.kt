package org.simple.clinic.user

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import org.intellij.lang.annotations.Language
import org.simple.clinic.facility.Facility
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.room.RoomEnumTypeConverter
import org.simple.clinic.util.room.SafeEnumTypeAdapter
import java.time.Instant
import java.util.UUID

// We intentionally have to keep the table name same because
// Room starts complaining if you try to rename a table which
// is referenced by another table in a foreign key (technically,
// SQLite supports renaming tables, but Room complains).
@Entity(tableName = "LoggedInUser")
@Parcelize
data class User(

    @PrimaryKey
    val uuid: UUID,

    val fullName: String,

    val phoneNumber: String,

    val pinDigest: String,

    val status: UserStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val loggedInStatus: LoggedInStatus,

    val registrationFacilityUuid: UUID,

    val currentFacilityUuid: UUID,

    val teleconsultPhoneNumber: String?,

    @Embedded(prefix = "capability_")
    val capabilities: Capabilities? // Update this to not null when API is finalized
) : Parcelable {

  val canSyncData: Boolean
    get() = loggedInStatus == LoggedInStatus.LOGGED_IN && status == UserStatus.ApprovedForSyncing

  val isWaitingForApproval: Boolean
    get() = status == UserStatus.WaitingForApproval

  val isApprovedForSyncing: Boolean
    get() = status == UserStatus.ApprovedForSyncing

  val isPendingSmsVerification: Boolean
    get() = loggedInStatus == LoggedInStatus.OTP_REQUESTED

  val isNotDisapprovedForSyncing: Boolean
    get() = status != UserStatus.DisapprovedForSyncing

  val isUserLoggedIn: Boolean
    get() = loggedInStatus == LoggedInStatus.LOGGED_IN

  fun withStatus(status: UserStatus, clock: UtcClock): User {
    return copy(status = status, updatedAt = Instant.now(clock))
  }

  fun withFullName(fullName: String): User {
    return copy(fullName = fullName)
  }

  fun withPhoneNumber(phoneNumber: String): User {
    return copy(phoneNumber = phoneNumber)
  }

  enum class LoggedInStatus {
    /**
     * Phone number match happened on the server,
     * and information was stored locally, but the
     * OTP request has not yet been made.
     **/
    NOT_LOGGED_IN,

    /**
     * Login OTP request has been raised with the server.
     **/
    OTP_REQUESTED,

    /**
     * Login OTP has been validated with the server
     * and the user is verified.
     */
    LOGGED_IN,

    /**
     * User has begun the reset PIN flow, but hasn't yet
     * submitted the PIN reset request to the server
     **/
    RESETTING_PIN,

    /**
     * User has raised a PIN reset request with the
     * server, but it has not yet been approved
     **/
    RESET_PIN_REQUESTED,

    /**
     * A network call starts returning 401 UNAUTHORIZED.
     *
     * This can happen in the following cases:
     *
     * - User's permission to sync has been revoked by the admin
     * - User has logged into a new device
     * - User has been moved to a different facility group by the admin
     **/
    UNAUTHORIZED;

    class RoomTypeConverter : RoomEnumTypeConverter<LoggedInStatus>(LoggedInStatus::class.java)

  }

  @JsonClass(generateAdapter = true)
  @Parcelize
  data class Capabilities(
      @Json(name = "can_teleconsult")
      val canTeleconsult: CapabilityStatus
  ) : Parcelable

  sealed class CapabilityStatus : Parcelable {

    @Parcelize
    object Yes : CapabilityStatus() {
      override fun toString(): String {
        return "yes"
      }
    }

    @Parcelize
    object No : CapabilityStatus() {
      override fun toString(): String {
        return "no"
      }
    }

    @Parcelize
    data class Unknown(val actual: String) : CapabilityStatus()

    object TypeAdapter : SafeEnumTypeAdapter<CapabilityStatus>(
        knownMappings = mapOf(
            Yes to "yes",
            No to "no"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actual }
    )

    class RoomTypeConverter {

      @TypeConverter
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(anEnum: CapabilityStatus?) = TypeAdapter.fromEnum(anEnum)
    }

    class MoshiTypeAdapter {
      @FromJson
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @ToJson
      fun fromEnum(anEnum: CapabilityStatus?) = TypeAdapter.fromEnum(anEnum)
    }
  }

  @Dao
  abstract class RoomDao {

    companion object {
      @Language("RoomSql")
      private const val CURRENT_FACILITY_QUERY = """
        SELECT 
         F.uuid, F.name, F.facilityType,
         F.streetAddress, F.villageOrColony, F.district,
         F.state, F.country, F.pinCode,
         F.protocolUuid, F.groupUuid,
         F.location_latitude, F.location_longitude,
         F.createdAt, F.updatedAt, F.deletedAt,
         F.syncStatus,
         F.config_diabetesManagementEnabled,
         F.config_teleconsultationEnabled,
         F.syncGroup
        FROM Facility F
        INNER JOIN LoggedInUser ON LoggedInUser.currentFacilityUuid = F.uuid
        LIMIT 1
      """
    }

    @Query("SELECT * FROM LoggedInUser LIMIT 1")
    abstract fun user(): Flowable<List<User>>

    @Query("SELECT * FROM LoggedInUser LIMIT 1")
    abstract fun userImmediate(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun createOrUpdate(user: User)

    @Query("UPDATE LoggedInUser SET loggedInStatus = :loggedInStatus WHERE uuid = :userUuId")
    abstract fun updateLoggedInStatusForUser(userUuId: UUID, loggedInStatus: LoggedInStatus)

    @Delete
    abstract fun deleteUser(user: User)

    @Query("SELECT COUNT(uuid) FROM LoggedInUser")
    abstract fun userCount(): Single<Int>

    @Query("UPDATE LoggedInUser SET currentFacilityUuid = :facilityUuid")
    abstract fun setCurrentFacility(facilityUuid: UUID): Int

    @Query(CURRENT_FACILITY_QUERY)
    abstract fun currentFacility(): Flowable<Facility>

    @Query(CURRENT_FACILITY_QUERY)
    abstract fun currentFacilityImmediate(): Facility?

    @Query("SELECT currentFacilityUuid FROM LoggedInUser LIMIT 1")
    abstract fun currentFacilityUuid(): UUID?

    @Query("""
      SELECT U.uuid userId, F.uuid currentFacilityId, F.syncGroup currentSyncGroupId
      FROM LoggedInUser U
      INNER JOIN Facility F ON U.currentFacilityUuid == F.uuid
    """)
    abstract fun userAndFacilityDetails(): UserFacilityDetails?
  }
}
