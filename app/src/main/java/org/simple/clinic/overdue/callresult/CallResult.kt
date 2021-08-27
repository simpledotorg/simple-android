package org.simple.clinic.overdue.callresult

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import java.util.UUID

@Entity(tableName = "CallResult")
data class CallResult(

    @PrimaryKey
    val id: UUID,

    val userId: UUID,

    val appointmentId: UUID,

    val removeReason: AppointmentCancelReason?,

    val outcome: Outcome,

    @Embedded
    val timestamps: Timestamps,

    val syncStatus: SyncStatus
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(callResults: List<CallResult>)
  }
}
