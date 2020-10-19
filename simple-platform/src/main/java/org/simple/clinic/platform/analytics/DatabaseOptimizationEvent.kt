package org.simple.clinic.platform.analytics

data class DatabaseOptimizationEvent(
    val sizeBeforeOptimizationBytes: Long,
    val sizeAfterOptimizationBytes: Long,
    val type: OptimizationType
) {

  enum class OptimizationType(val analyticsName: String) {
    PurgeDeleted("Purge Deleted"),
    PurgeFromOtherSyncGroup("Purge From Other Sync Group")
  }
}
