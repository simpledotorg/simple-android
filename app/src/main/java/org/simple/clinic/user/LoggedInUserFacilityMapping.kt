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
)
