package org.simple.clinic.overdue

import android.database.Cursor
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.sqlite.db.SimpleSQLiteQuery
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.reactivex.Flowable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.summary.nextappointment.NextAppointmentPatientProfile
import org.simple.clinic.util.room.SafeEnumTypeAdapter
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "Appointment",
    indices = [
      Index("patientUuid", unique = false),
      Index("creationFacilityUuid", unique = false),
      Index("facilityUuid", unique = false)
    ]
)
@Parcelize
data class Appointment(
    @PrimaryKey val uuid: UUID,
    val patientUuid: UUID,
    val facilityUuid: UUID,
    val scheduledDate: LocalDate,
    val status: Status,
    val cancelReason: AppointmentCancelReason?,
    val remindOn: LocalDate?,
    val agreedToVisit: Boolean?,
    val appointmentType: AppointmentType,
    val syncStatus: SyncStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
    val creationFacilityUuid: UUID?
) : Parcelable {

  fun wasCancelledBecauseOfInvalidPhoneNumber(): Boolean = status == Status.Cancelled && cancelReason == AppointmentCancelReason.InvalidPhoneNumber

  sealed class Status : Parcelable {

    @Parcelize
    data object Scheduled : Status()

    @Parcelize
    data object Cancelled : Status()

    @Parcelize
    data object Visited : Status()

    @Parcelize
    data class Unknown(val actualValue: String) : Status()

    object TypeAdapter : SafeEnumTypeAdapter<Status>(
        knownMappings = mapOf(
            Scheduled to "scheduled",
            Cancelled to "cancelled",
            Visited to "visited"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actualValue }
    )

    class RoomTypeConverter {
      @TypeConverter
      fun toEnum(value: String?): Status? = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(reason: Status?): String? = TypeAdapter.fromEnum(reason)
    }

    class MoshiTypeConverter {
      @FromJson
      fun toEnum(value: String?): Status? = TypeAdapter.toEnum(value)

      @ToJson
      fun fromEnum(reason: Status?): String? = TypeAdapter.fromEnum(reason)
    }

    companion object {
      @VisibleForTesting
      fun random(): Status = TypeAdapter.knownMappings.keys.shuffled().first()
    }
  }

  sealed class AppointmentType : Parcelable {

    @Parcelize
    data object Manual : AppointmentType()

    @Parcelize
    data object Automatic : AppointmentType()

    @Parcelize
    data class Unknown(val actual: String) : AppointmentType()

    object TypeAdapter : SafeEnumTypeAdapter<AppointmentType>(
        knownMappings = mapOf(
            Manual to "manual",
            Automatic to "automatic"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actual }
    )

    class RoomTypeConverter {

      @TypeConverter
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(enum: AppointmentType?) = TypeAdapter.fromEnum(enum)
    }

    class MoshiTypeAdapter {

      @FromJson
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @ToJson
      fun fromEnum(enum: AppointmentType) = TypeAdapter.fromEnum(enum)
    }

    companion object {
      @VisibleForTesting
      fun random() = AppointmentType.TypeAdapter.knownMappings.keys.shuffled().first()
    }
  }

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM Appointment WHERE syncStatus = :status")
    fun recordsWithSyncStatus(status: SyncStatus): List<Appointment>

    @Query("""
        SELECT * FROM appointment
        WHERE syncStatus = :syncStatus
        LIMIT :limit OFFSET :offset
    """)
    fun recordsWithSyncStatusBatched(
        syncStatus: SyncStatus,
        limit: Int,
        offset: Int
    ): List<Appointment>

    @Query("UPDATE Appointment SET syncStatus = :to WHERE syncStatus = :from")
    fun updateSyncStatus(from: SyncStatus, to: SyncStatus)

    @RawQuery
    fun updateSyncStatusForIdsRaw(query: SimpleSQLiteQuery): Int

    fun updateSyncStatusForIds(ids: List<UUID>, to: SyncStatus) {
      updateSyncStatusForIdsRaw(SimpleSQLiteQuery(
          "UPDATE Appointment SET syncStatus = '$to' WHERE uuid IN (${ids.joinToString(prefix = "'", postfix = "'", separator = "','")})"
      ))
    }

    @Query("SELECT * FROM Appointment WHERE uuid = :id")
    fun getOne(id: UUID): Appointment?

    @Query("SELECT uuid FROM Appointment WHERE syncStatus = :syncStatus")
    fun recordIdsWithSyncStatus(syncStatus: SyncStatus): List<UUID>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(appointments: List<Appointment>)

    @Query("""
      SELECT * FROM Appointment
      WHERE patientUuid = :patientUuid
      ORDER BY createdAt DESC
      LIMIT 1
    """)
    fun lastCreatedAppointmentForPatient(patientUuid: UUID): Appointment?

    @Query("SELECT COUNT(uuid) FROM Appointment")
    fun count(): Flowable<Int>

    @Query("SELECT COUNT(uuid) FROM Appointment WHERE syncStatus = :syncStatus")
    fun countWithStatus(syncStatus: SyncStatus): Flowable<Int>

    @Query("""
      UPDATE Appointment
      SET status = :updatedStatus, syncStatus = :newSyncStatus, updatedAt = :newUpdatedAt
      WHERE patientUuid = :patientUuid AND status = :scheduledStatus
    """)
    fun markOlderAppointmentsAsVisited(
        patientUuid: UUID,
        updatedStatus: Status,
        scheduledStatus: Status,
        newSyncStatus: SyncStatus,
        newUpdatedAt: Instant
    )

    @Query("""
       UPDATE Appointment
       SET remindOn = :reminderDate, syncStatus = :newSyncStatus, updatedAt = :newUpdatedAt
       WHERE uuid = :appointmentUUID
    """)
    fun saveRemindDate(
        appointmentUUID: UUID,
        reminderDate: LocalDate,
        newSyncStatus: SyncStatus,
        newUpdatedAt: Instant
    )

    @Query("""
      UPDATE Appointment
      SET remindOn = :reminderDate, agreedToVisit = :agreed, syncStatus = :newSyncStatus, updatedAt = :newUpdatedAt
      WHERE uuid = :appointmentUUID
    """)
    fun markAsAgreedToVisit(
        appointmentUUID: UUID,
        reminderDate: LocalDate,
        agreed: Boolean = true,
        newSyncStatus: SyncStatus,
        newUpdatedAt: Instant
    )

    @Query("""
      UPDATE Appointment
      SET status = :newStatus, syncStatus = :newSyncStatus, updatedAt = :newUpdatedAt
      WHERE uuid = :appointmentUuid
    """)
    fun markAsVisited(
        appointmentUuid: UUID,
        newStatus: Status,
        newSyncStatus: SyncStatus,
        newUpdatedAt: Instant
    )

    @Query("""
      UPDATE Appointment
      SET cancelReason = :cancelReason, status = :newStatus, syncStatus = :newSyncStatus, updatedAt = :newUpdatedAt
      WHERE uuid = :appointmentUuid
    """)
    fun cancelWithReason(
        appointmentUuid: UUID,
        cancelReason: AppointmentCancelReason,
        newStatus: Status,
        newSyncStatus: SyncStatus,
        newUpdatedAt: Instant
    )

    @Query("DELETE FROM Appointment")
    fun clear()

    @Query("""
      UPDATE Appointment SET status = :updatedStatus, syncStatus = :newSyncStatus, updatedAt = :newUpdatedAt
      WHERE patientUuid = :patientUuid AND status = :scheduledStatus AND createdAt < :createdBefore
    """)
    fun markAsVisitedForPatient(
        patientUuid: UUID,
        updatedStatus: Status,
        scheduledStatus: Status,
        newSyncStatus: SyncStatus,
        newUpdatedAt: Instant,
        createdBefore: Instant
    )

    @Query("""
      DELETE FROM Appointment
      WHERE deletedAt IS NOT NULL AND syncStatus == 'DONE'
    """)
    fun purgeDeleted(): Int

    @Query("""
      DELETE FROM Appointment
      WHERE uuid NOT IN (
        SELECT uuid
        FROM Appointment
        GROUP BY patientUuid HAVING MAX(createdAt)
      ) AND syncStatus == 'DONE'
    """)
    fun purgeUnusedAppointments(): Int

    @Query(""" SELECT * FROM Appointment """)
    fun getAllAppointments(): List<Appointment>

    @Query(""" SELECT * FROM Appointment WHERE patientUuid = :patientUuid """)
    fun getAllAppointmentsForPatient(patientUuid: UUID): List<Appointment>

    @Query("""
        DELETE FROM Appointment
        WHERE 
            uuid NOT IN (
                SELECT A.uuid FROM Appointment A
                INNER JOIN Patient P ON P.uuid == A.patientUuid
            ) AND
            syncStatus == 'DONE'
    """)
    fun deleteWithoutLinkedPatient()


    @Query("""
        DELETE FROM Appointment
        WHERE patientUuid IN (
		        SELECT A.patientUuid 
			      FROM Appointment A
			      LEFT JOIN Patient P ON P.uuid == A.patientUuid
			      WHERE P.uuid IS NULL AND A.syncStatus == 'DONE'
		    )
    """)
    fun purgeAppointmentsWhenPatientIsNull(): Int

    @Query("""
      SELECT * FROM Appointment
      WHERE 
        patientUuid = :patientUUID
        AND scheduledDate < :scheduledDate
        AND deletedAt IS NULL 
        AND status IS NOT 'visited'
      ORDER BY createdAt DESC LIMIT 1
    """)
    fun latestOverdueAppointmentForPatient(
        patientUUID: UUID,
        scheduledDate: LocalDate
    ): Appointment?

    @Query("""
      SELECT * FROM Appointment
      WHERE 
        patientUuid = :patientUuid AND
        deletedAt IS NULL AND
        status = 'scheduled' AND
        appointmentType != 'automatic'
      GROUP BY patientUuid HAVING MAX(scheduledDate)
    """)
    @Transaction
    fun nextAppointmentPatientProfile(patientUuid: UUID): NextAppointmentPatientProfile?

    @Query("""
        SELECT (
            CASE
                WHEN (COUNT(uuid) > 0) THEN 1
                ELSE 0
            END
        )
        FROM Appointment
        WHERE 
        updatedAt > :instantToCompare AND
        status = 'scheduled' AND 
        patientUuid = :patientUuid
    """)
    fun hasAppointmentForPatientChangedSince(patientUuid: UUID, instantToCompare: Instant): Boolean

    @Query("""
      SELECT * FROM Appointment
      WHERE 
        patientUuid = :patientUuid
        AND deletedAt IS NULL AND status = 'scheduled'
      GROUP BY patientUuid HAVING MAX(createdAt)
    """)
    fun latestScheduledAppointmentForPatient(patientUuid: UUID): Appointment?

    @Query("""
      SELECT
       P.createdAt patientCreatedAt,
       BI.identifier identifierValue,
       P.fullName patientName,
       P.gender patientGender,
       P.age_value patientAgeValue,
       P.age_updatedAt patientAgeUpdatedAt,
       P.dateOfBirth patientDateOfBirth,
       PA.streetAddress patientStreetAddress,
       PA.colonyOrVillage patientColonyOrVillage,
       A.scheduledDate appointmentScheduledAt,
       PPN.number patientPhoneNumber,
       (
         SELECT GROUP_CONCAT((name || " " || dosage), ", ") nameAndDosage FROM PrescribedDrug
         WHERE patientUuid = P.uuid AND isDeleted = 0
       ) prescribedDrugs
      FROM Appointment A
      INNER JOIN Patient P ON P.uuid = A.patientUuid
      LEFT JOIN PatientAddress PA ON PA.uuid = P.addressUuid
      LEFT JOIN (
        SELECT * FROM PatientPhoneNumber
        WHERE deletedAt IS NULL
        GROUP BY patientUuid HAVING MAX(createdAt)
      ) PPN ON PPN.patientUuid = P.uuid AND PPN.deletedAt IS NULL
      LEFT JOIN (
        SELECT * FROM BusinessId
        WHERE identifierType = "simple_bp_passport" AND deletedAt IS NULL
        GROUP BY patientUuid HAVING MAX(createdAt)
      ) BI ON BI.patientUuid = P.uuid
      WHERE A.uuid IN (:ids)
    """)
    fun appointmentAndPatientInformationForIds(ids: List<UUID>): Cursor
  }
}
