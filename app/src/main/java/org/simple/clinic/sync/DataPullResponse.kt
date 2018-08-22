package org.simple.clinic.sync

import org.threeten.bp.Instant

interface DataPullResponse<T : SynceablePayload<out Synceable>> {

  val payloads: List<T>

  val processedSinceTimestamp: Instant
}
