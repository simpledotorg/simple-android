package org.resolvetosavelives.red.search

import org.resolvetosavelives.red.util.RoomEnumTypeConverter

enum class SyncStatus {
  PENDING,
  IN_FLIGHT,
  DONE,
  INVALID;

  class RoomTypeConvert : RoomEnumTypeConverter<SyncStatus>(SyncStatus::class.java)
}

/**
 * This is an extension function so that null status can also be handled.
 */
fun SyncStatus?.canBeOverriddenByServerCopy(): Boolean {
  return when (this) {
    SyncStatus.PENDING -> false
    SyncStatus.IN_FLIGHT -> false
    SyncStatus.INVALID -> true
    SyncStatus.DONE -> true
    null -> true
  }
}
