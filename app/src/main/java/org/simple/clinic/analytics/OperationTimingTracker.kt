package org.simple.clinic.analytics

import androidx.annotation.VisibleForTesting
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant

/**
 * This class is meant to collect timing information for an operation over time. An operation can
 * have multiple stages, and it allows collecting discrete timing information for each
 * of these stages.
 *
 * ### Example Usage
 * ```
 * val timingTracker = OperationTimingTracker("Search for patients", utcClock)
 *
 * timingTracker.start("fetch-patient-names")
 * // Fetch patient names from database
 * timingTracker.stop("fetch-patient-names")
 *
 * timingTracker.start("fuzzy-name-matching")
 * // Use fuzzy name matching to drop patients that don't match  criteria
 * timingTracker.stop("fuzzy-name-matching")
 *
 * timingTracker.start("fetch-patient-details")
 * // Fetch complete patient details from database
 * timingTracker.stop("fetch-patient-details")
 *
 * timingTracker.start("sort-by-facility")
 * // Sort the final search results list according the patients visited facility list
 * timingTracker.stop("stop-by-facility")
 * ```
 *
 * **Note:** Since an operation can happen across multiple threads, this class has been designed
 * with thread-safety in mind. So it uses immutable maps for tracking operations, and atomic
 * primitives where necessary.
 **/
class OperationTimingTracker(
    val name: String,
    private val clock: UtcClock
) {
  @get:VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  var ongoing: Map<String, Instant> = emptyMap()
    private set

  fun start(stageName: String) {
    ongoing += "$name:$stageName" to Instant.now(clock)
  }

  fun stop(stageName: String) {
    val analyticsEventName = "$name:$stageName"
    ongoing[analyticsEventName]?.let { stageStartedAt ->
      val now = Instant.now(clock)
      val timeTakenToCompleteStage = Duration.between(stageStartedAt, now).abs()

      ongoing -= analyticsEventName

      Analytics.reportTimeTaken(analyticsEventName, timeTakenToCompleteStage)
    }
  }
}
