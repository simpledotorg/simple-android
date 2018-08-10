package org.simple.clinic.user

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import org.simple.clinic.facility.Facility
import java.util.UUID

@Entity(
    tableName = "LoggedInUserFacilityMapping",
    foreignKeys = [
      ForeignKey(
          entity = LoggedInUser::class,
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
    open fun insert(user: LoggedInUser, facilityIds: List<UUID>, currentFacilityUuid: UUID) {
      val mappings = facilityIds
          .map {
            LoggedInUserFacilityMapping(
                userUuid = user.uuid,
                facilityUuid = it,
                isCurrentFacility = it == currentFacilityUuid)
          }
      insert(mappings)
      setCurrentFacility(user.uuid, currentFacilityUuid)
    }

    @Insert
    abstract fun insert(mappings: List<LoggedInUserFacilityMapping>)

    // TODO: Test that a user only has one facility with isCurrentFacility=true.
    @Query("""
      UPDATE LoggedInUserFacilityMapping
      SET isCurrentFacility = 'TRUE'
      WHERE userUuid = :userUuid AND facilityUuid = :facilityUuid
      """)
    abstract fun setCurrentFacility(userUuid: UUID, facilityUuid: UUID)

    @Query("""
      SELECT *
      FROM Facility
      INNER JOIN LoggedInUserFacilityMapping ON LoggedInUserFacilityMapping.facilityUuid = Facility.uuid
      WHERE LoggedInUserFacilityMapping.userUuid = :userUuid
      LIMIT 1
      """)
    abstract fun currentFacility(userUuid: UUID): Flowable<Facility>

    @Query("SELECT facilityUuid FROM LoggedInUserFacilityMapping WHERE userUuid = :userUuid ")
    abstract fun facilityUuidsFor(userUuid: UUID): Flowable<List<UUID>>
  }
}
