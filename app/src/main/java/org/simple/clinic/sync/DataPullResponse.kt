package org.simple.clinic.sync

import org.threeten.bp.Instant

interface DataPullResponse<T> {

  val payloads: List<T>

  val processedSinceTimestamp: Instant
}
