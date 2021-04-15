package org.simple.clinic.storage

import android.os.Parcelable
import androidx.room.Embedded
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.UtcClock
import java.time.Instant

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
@Parcelize
data class Timestamps(
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?
) : Parcelable {

  companion object {

    fun create(clock: UtcClock): Timestamps {
      val instant = Instant.now(clock)

      return Timestamps(createdAt = instant, updatedAt = instant, deletedAt = null)
    }
  }

  fun update(clock: UtcClock): Timestamps {
    return copy(updatedAt = Instant.now(clock))
  }

  fun delete(clock: UtcClock): Timestamps {
    val instant = Instant.now(clock)
    return copy(updatedAt = instant, deletedAt = instant)
  }
}
