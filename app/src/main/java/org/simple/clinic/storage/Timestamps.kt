package org.simple.clinic.storage

import androidx.room.Embedded
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant

/**
 * There are three timestamps that are always present in every single synced model:
 *
 * - `createdAt`: When the database record was created.
 * - `updatedAt`: When the database record was updated.
 * - `deletedAt`: When the database record was deleted.
 *
 * All timestamps are in UTC.
 *
 * This class is meant to encapsulate these timestamps as an [Embedded] model in the database
 * entities.
 **/
data class Timestamps(
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?
) {

  companion object {

    fun now(clock: UtcClock): Timestamps {
      val instant = Instant.now(clock)

      return Timestamps(createdAt = instant, updatedAt = instant, deletedAt = null)
    }
  }

  fun updated(clock: UtcClock): Timestamps {
    return copy(updatedAt = Instant.now(clock))
  }

  fun deleted(clock: UtcClock): Timestamps {
    val instant = Instant.now(clock)
    return copy(updatedAt = instant, deletedAt = instant)
  }
}
