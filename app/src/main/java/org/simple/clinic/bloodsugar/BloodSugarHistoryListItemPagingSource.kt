package org.simple.clinic.bloodsugar

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
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
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
class BloodSugarHistoryListItemPagingSource @AssistedInject constructor(
    private val appDatabase: AppDatabase,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val schedulersProvider: SchedulersProvider,
    @Named("full_date") private val dateFormatter: DateTimeFormatter,
    @Named("time_for_measurement_history") private val timeFormatter: DateTimeFormatter,
    @Assisted private val canEditFor: Duration,
    @Assisted private val source: PagingSource<Int, BloodSugarMeasurement>,
    @Assisted private val bloodSugarUnitPreference: BloodSugarUnitPreference
) : PagingSource<Int, BloodSugarHistoryListItem>() {

  @AssistedFactory
  interface Factory {
    fun create(
        canEditFor: Duration,
        bloodSugarUnitPreference: BloodSugarUnitPreference,
        source: PagingSource<Int, BloodSugarMeasurement>,
    ): BloodSugarHistoryListItemPagingSource
  }

  private val observer = ThreadSafeInvalidationObserver(
      tables = arrayOf(BloodSugarMeasurement.TABLE_NAME),
      onInvalidated = ::invalidate
  )

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BloodSugarHistoryListItem> {
    return withContext(schedulersProvider.io().asCoroutineDispatcher()) {
      observer.registerIfNecessary(db = appDatabase)
      try {
        when (val sourceLoadResult = source.load(params)) {
          is LoadResult.Page -> {
            val bloodSugarMeasurements = sourceLoadResult.data
            val measurementListItems = convertToBloodSugarHistoryListItems(bloodSugarMeasurements)
            LoadResult.Page(
                data = measurementListItems,
                prevKey = sourceLoadResult.prevKey,
                nextKey = sourceLoadResult.nextKey,
            )
          }

          is LoadResult.Error -> LoadResult.Error(sourceLoadResult.throwable)
          is LoadResult.Invalid -> {
            @Suppress("UNCHECKED_CAST")
            INVALID as LoadResult.Invalid<Int, BloodSugarHistoryListItem>
          }
        }
      } catch (e: Exception) {
        LoadResult.Error(e)
      }
    }
  }

  private fun convertToBloodSugarHistoryListItems(measurements: List<BloodSugarMeasurement>): List<BloodSugarHistoryListItem> {
    val measurementsByDate = measurements.groupBy { it.recordedAt.toLocalDateAtZone(userClock.zone) }

    return measurementsByDate.mapValues { (_, measurementsList) ->
      val hasMultipleMeasurementsInSameDate = measurementsList.size > 1
      val now = Instant.now(utcClock)

      measurementsList.map { measurement ->
        val isBloodSugarEditable = isBloodSugarEditable(now, measurement, canEditFor)
        val recordedAt = measurement.recordedAt.toLocalDateAtZone(userClock.zone)
        val bloodSugarTime = if (hasMultipleMeasurementsInSameDate) {
          timeFormatter.format(measurement.recordedAt.atZone(userClock.zone))
        } else {
          null
        }

        BloodSugarHistoryListItem.BloodSugarHistoryItem(
            measurement = measurement,
            bloodSugarDate = dateFormatter.format(recordedAt),
            bloodSugarTime = bloodSugarTime,
            isBloodSugarEditable = isBloodSugarEditable,
            bloodSugarUnitPreference = bloodSugarUnitPreference
        )
      }
    }.values.flatten()
  }

  private fun isBloodSugarEditable(
      now: Instant,
      bloodSugarMeasurement: BloodSugarMeasurement,
      bloodSugarEditableFor: Duration
  ): Boolean {
    val createdAt = bloodSugarMeasurement.timestamps.createdAt

    val durationSinceBloodSugarCreated = Duration.between(createdAt, now)

    return durationSinceBloodSugarCreated <= bloodSugarEditableFor
  }

  override fun getRefreshKey(state: PagingState<Int, BloodSugarHistoryListItem>): Int? {
    return state.getClippedRefreshKey()
  }
}
