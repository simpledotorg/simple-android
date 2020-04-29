package org.simple.clinic.sync

enum class BatchSize(val numberOfRecords: Int) {
  VERY_SMALL(10),
  SMALL(150),
  MEDIUM(500),
  LARGE(1000)
}
