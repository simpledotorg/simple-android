package org.simple.clinic.bp

import android.annotation.SuppressLint
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import androidx.room.paging.util.ThreadSafeInvalidationObserver
import androidx.room.paging.util.getClippedRefreshKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.BloodPressureHistoryItem
import org.simple.clinic.util.INVALID
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
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
) : RxPagingSource<Int, BloodPressureHistoryListItem>() {

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

  override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, BloodPressureHistoryListItem>> {
    observer.registerIfNecessary(db = appDatabase)
    return try {
      val sourceLoadResultSingle = rxSingle { source.load(params) }
      return sourceLoadResultSingle
          .observeOn(schedulersProvider.io())
          .map { sourceLoadResult ->
            when (sourceLoadResult) {
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
          }
    } catch (e: Exception) {
      Single.just(LoadResult.Error(e))
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
