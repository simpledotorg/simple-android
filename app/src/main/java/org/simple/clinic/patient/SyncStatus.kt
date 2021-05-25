package org.simple.clinic.patient

import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.INVALID
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.util.room.RoomEnumTypeConverter

enum class SyncStatus {
  PENDING,
  DONE,
  INVALID;

  class RoomTypeConverter : RoomEnumTypeConverter<SyncStatus>(SyncStatus::class.java)
}

/**
 * This is an extension function so that null status can also be handled.
 */
fun SyncStatus?.canBeOverriddenByServerCopy(): Boolean {
  return when (this) {
    PENDING -> false
    INVALID, DONE, null -> true
  }
}
