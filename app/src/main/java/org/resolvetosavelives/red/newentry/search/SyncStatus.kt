package org.resolvetosavelives.red.newentry.search

import org.resolvetosavelives.red.util.RoomEnumTypeConverter

enum class SyncStatus {
  PENDING,
  IN_FLIGHT,
  DONE,
  INVALID;

  class RoomTypeConvert : RoomEnumTypeConverter<SyncStatus>(SyncStatus::class.java)
}
