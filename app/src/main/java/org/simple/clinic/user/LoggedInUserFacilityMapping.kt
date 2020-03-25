package org.simple.clinic.user

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable
import org.simple.clinic.facility.Facility
import java.util.UUID

@Entity(
    tableName = "LoggedInUserFacilityMapping",
    foreignKeys = [
      ForeignKey(
          entity = User::class,
          parentColumns = ["uuid"],
          childColumns = ["userUuid"]),
      ForeignKey(
          entity = Facility::class,
          parentColumns = ["uuid"],
          childColumns = ["facilityUuid"])
    ],
    primaryKeys = ["userUuid", "facilityUuid"],
    indices = [(Index("facilityUuid"))])  // userUuid gets an implicit index.
data class LoggedInUserFacilityMapping(

    val userUuid: UUID,

    val facilityUuid: UUID,

    val isCurrentFacility: Boolean
) {

  @Dao
  abstract class RoomDao {

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
