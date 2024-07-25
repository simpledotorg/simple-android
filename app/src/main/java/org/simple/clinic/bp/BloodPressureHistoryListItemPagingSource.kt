package org.simple.clinic.bp

import android.annotation.SuppressLint
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.paging.util.ThreadSafeInvalidationObserver
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.BloodPressureHistoryItem
import org.simple.clinic.util.INVALID
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.getClippedRefreshKey
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Named

@SuppressLint("RestrictedApi")
class BloodPressureHistoryListItemPagingSource @AssistedInject constructor(
    private val appDatabase: AppDatabase,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val schedulersProvider: SchedulersProvider,
    @Named("full_date") private val dateFormatter: DateTimeFormatter,
    @Named("time_for_measurement_history") private val timeFormatter: DateTimeFormatter,
    @Assisted private val bpEditableDuration: Duration,
    @Assisted private val source: PagingSource<Int, BloodPressureMeasurement>
) : PagingSource<Int, BloodPressureHistoryListItem>() {

  @AssistedFactory
  interface Factory {
    fun create(
        bpEditableDuration: Duration,
        source: PagingSource<Int, BloodPressureMeasurement>
    ): BloodPressureHistoryListItemPagingSource
  }

  private val observer = ThreadSafeInvalidationObserver(
      tables = arrayOf(BloodPressureMeasurement.TABLE_NAME),
      onInvalidated = ::invalidate
  )

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BloodPressureHistoryListItem> {
    return withContext(schedulersProvider.io().asCoroutineDispatcher()) {
      observer.registerIfNecessary(db = appDatabase)
      try {
        when (val sourceLoadResult = source.load(params)) {
          is LoadResult.Page -> {
            val bloodPressureMeasurements = sourceLoadResult.data
            val measurementListItems = convertToBloodPressureHistoryListItems(bloodPressureMeasurements)
            LoadResult.Page(
                data = measurementListItems,
                prevKey = sourceLoadResult.prevKey,
                nextKey = sourceLoadResult.nextKey,
            )
          }

          is LoadResult.Error -> LoadResult.Error(sourceLoadResult.throwable)
          is LoadResult.Invalid -> {
            @Suppress("UNCHECKED_CAST")
            INVALID as LoadResult.Invalid<Int, BloodPressureHistoryListItem>
          }
        }
      } catch (e: Exception) {
        LoadResult.Error(e)
      }
    }
  }

  private fun convertToBloodPressureHistoryListItems(measurements: List<BloodPressureMeasurement>): List<BloodPressureHistoryListItem> {
    val measurementsByDate = measurements.groupBy { it.recordedAt.toLocalDateAtZone(userClock.zone) }

    return measurementsByDate.mapValues { (_, measurementsList) ->
      val hasMultipleMeasurementsInSameDate = measurementsList.size > 1
      val now = Instant.now(utcClock)

      measurementsList.map { measurement ->
        val isBpEditable = isBpEditable(now, measurement, bpEditableDuration)
        val recordedAt = measurement.recordedAt.toLocalDateAtZone(userClock.zone)
        val bpTime = if (hasMultipleMeasurementsInSameDate) {
          timeFormatter.format(measurement.recordedAt.atZone(userClock.zone))
        } else {
          null
        }

        BloodPressureHistoryItem(
            measurement = measurement,
            isBpEditable = isBpEditable,
            isBpHigh = measurement.level.isHigh,
            bpDate = dateFormatter.format(recordedAt),
            bpTime = bpTime
        )
      }
    }.values.flatten()
  }

  private fun isBpEditable(
      now: Instant,
      bloodPressureMeasurement: BloodPressureMeasurement,
      bpEditableFor: Duration
  ): Boolean {
    val createdAt = bloodPressureMeasurement.createdAt

    val durationSinceBpCreated = Duration.between(createdAt, now)

    return durationSinceBpCreated <= bpEditableFor
  }

  override fun getRefreshKey(state: PagingState<Int, BloodPressureHistoryListItem>): Int? {
    return state.getClippedRefreshKey()
  }
}
