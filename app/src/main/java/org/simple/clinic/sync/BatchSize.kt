package org.simple.clinic.sync

enum class BatchSize(val numberOfRecords: Int) {
  VERY_SMALL(10),
  SMALL(50),
  MEDIUM(250),
  LARGE(500)
}
