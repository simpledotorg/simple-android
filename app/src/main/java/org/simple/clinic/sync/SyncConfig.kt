package org.simple.clinic.sync

import org.threeten.bp.Duration

data class SyncConfig(

    val frequency: Duration,

    val backOffDelay: Duration,

    val batchSizeEnum: BatchSize
) {
  val batchSize = batchSizeEnum.numberOfRecords
}
