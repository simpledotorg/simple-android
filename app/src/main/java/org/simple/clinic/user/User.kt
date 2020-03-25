package org.simple.clinic.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable
import io.reactivex.Single
import org.simple.clinic.facility.Facility
import org.simple.clinic.util.room.RoomEnumTypeConverter
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import java.util.UUID

// We intentionally have to keep the table name same because
// Room starts complaining if you try to rename a table which
// is referenced by another table in a foreign key (technically,
// SQLite supports renaming tables, but Room complains).
@Entity(tableName = "LoggedInUser")
data class User(

    @PrimaryKey
    val uuid: UUID,

    val fullName: String,

    val phoneNumber: String,

    val pinDigest: String,

    val status: UserStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val loggedInStatus: LoggedInStatus
) {

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

  @Dao
  abstract class RoomDao {

    @Query("SELECT * FROM LoggedInUser LIMIT 1")
    abstract fun user(): Flowable<List<User>>

    @Query("SELECT * FROM LoggedInUser LIMIT 1")
    abstract fun userImmediate(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun createOrUpdate(user: User)

    @Query("UPDATE LoggedInUser SET loggedInStatus = :loggedInStatus WHERE uuid = :userUuId")
    abstract fun updateLoggedInStatusForUser(userUuId: UUID, loggedInStatus: LoggedInStatus)

    @Delete
    protected abstract fun deleteUser(user: User)

    @Transaction
    open fun deleteUserAndFacilityMappings(user: User) {
      deleteMappingsForUser(user.uuid)
      deleteUser(user)
    }

    @Query("SELECT COUNT(uuid) FROM LoggedInUser")
    abstract fun userCount(): Single<Int>

    @Transaction
    open fun insertOrUpdateFacilitiesForUser(user: User, facilityIds: List<UUID>) {
      val mappings = facilityIds
          .map {
            LoggedInUserFacilityMapping(
                userUuid = user.uuid,
                facilityUuid = it,
                isCurrentFacility = false)
          }
      insertOrUpdateFacilitiesForUser(mappings)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrUpdateFacilitiesForUser(mappings: List<LoggedInUserFacilityMapping>)

    @Transaction
    open fun changeCurrentFacility(userUuid: UUID, newCurrentFacilityUuid: UUID) {
      val oldCurrentFacilityUuid = currentFacilityUuid(userUuid)
      if (oldCurrentFacilityUuid != null) {
        setFacilityIsCurrent(userUuid, oldCurrentFacilityUuid, isCurrent = false)
      }
      val updatedRows = setFacilityIsCurrent(userUuid, newCurrentFacilityUuid, isCurrent = true)
      if (updatedRows != 1) {
        throw AssertionError("Couldn't update current facility. A mapping between $userUuid and $newCurrentFacilityUuid probably does not exist.")
      }
    }

    @Query("""
      UPDATE LoggedInUserFacilityMapping
      SET isCurrentFacility = :isCurrent
      WHERE userUuid = :userUuid AND facilityUuid = :facilityUuid
      """)
    protected abstract fun setFacilityIsCurrent(userUuid: UUID, facilityUuid: UUID, isCurrent: Boolean): Int

    @Query("""
      SELECT * FROM Facility
      INNER JOIN LoggedInUserFacilityMapping ON LoggedInUserFacilityMapping.facilityUuid = Facility.uuid
      WHERE LoggedInUserFacilityMapping.userUuid = :userUuid
      AND LoggedInUserFacilityMapping.isCurrentFacility = 1
      LIMIT 1
      """)
    abstract fun currentFacility(userUuid: UUID): Flowable<Facility>

    @Query("""
      SELECT * FROM Facility
      INNER JOIN LoggedInUserFacilityMapping ON LoggedInUserFacilityMapping.facilityUuid = Facility.uuid
      WHERE LoggedInUserFacilityMapping.userUuid = :userUuid
      AND LoggedInUserFacilityMapping.isCurrentFacility = 1
      LIMIT 1
      """)
    abstract fun currentFacilityImmediate(userUuid: UUID): Facility?

    @Query("""
      SELECT facilityUuid FROM LoggedInUserFacilityMapping
      WHERE userUuid = :userUuid
      AND isCurrentFacility = 1
    """)
    abstract fun currentFacilityUuid(userUuid: UUID): UUID?

    @Query("SELECT * FROM LoggedInUserFacilityMapping WHERE userUuid = :userUuid")
    abstract fun mappingsForUser(userUuid: UUID): Flowable<List<LoggedInUserFacilityMapping>>

    @Query("DELETE FROM LoggedInUserFacilityMapping WHERE userUuid = :userUuid")
    abstract fun deleteMappingsForUser(userUuid: UUID)
  }
}
