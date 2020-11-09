package org.simple.clinic.bloodsugar

import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import androidx.paging.toObservable
import androidx.room.InvalidationTracker
import com.f2prateek.rx.preferences2.Preference
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.simple.clinic.AppDatabase
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem.BloodSugarHistoryItem
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem.NewBloodSugarButton
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfig
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toLocalDateAtZone
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Named

class BloodSugarHistoryListItemDataSource(
    private val appDatabase: AppDatabase,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val dateFormatter: DateTimeFormatter,
    private val timeFormatter: DateTimeFormatter,
    private val canEditFor: Duration,
    private val source: PositionalDataSource<BloodSugarMeasurement>,
    private val bloodSugarUnitPreference: BloodSugarUnitPreference
) : PositionalDataSource<BloodSugarHistoryListItem>() {

  private val invalidationTracker = object : InvalidationTracker.Observer(arrayOf("BloodSugarMeasurements")) {
    override fun onInvalidated(tables: MutableSet<String>) {
      invalidate()
    }
  }

  init {
    appDatabase.invalidationTracker.addObserver(invalidationTracker)
  }

  override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<BloodSugarHistoryListItem>) {
    // we are subtracting 1 from load size and page size to avoid the source data source
    // from using the params because we will be adding a header to the BloodSugarHistoryListItemDataSource list, which
    // is not present in the source data source
    val loadParamsForDatabaseSource = LoadRangeParams(params.startPosition - 1, params.loadSize)
    source.loadRange(loadParamsForDatabaseSource, object : LoadRangeCallback<BloodSugarMeasurement>() {
      override fun onResult(measurements: MutableList<BloodSugarMeasurement>) {
        val measurementListItems = convertToBloodSugarHistoryListItems(measurements)
        val data = if (params.startPosition == 0) {
          listOf(NewBloodSugarButton) + measurementListItems
        } else {
          measurementListItems
        }
        callback.onResult(data)
      }
    })
  }

  override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<BloodSugarHistoryListItem>) {
    // we are subtracting 1 from load size and page size to avoid the source data source
    // from using the params because we will be adding a header to the BloodSugarHistoryListItemDataSource list, which
    // is not present in the source data source
    val loadParamsForDatabaseSource = LoadInitialParams(
        params.requestedStartPosition,
        params.requestedLoadSize - 1,
        params.pageSize - 1,
        params.placeholdersEnabled
    )
    source.loadInitial(loadParamsForDatabaseSource, object : LoadInitialCallback<BloodSugarMeasurement>() {
      override fun onResult(measurements: MutableList<BloodSugarMeasurement>, position: Int, totalCount: Int) {
        // Adding 1 to the total count so that BloodSugarHistoryListItemDataSource knows that we are
        // adding a another item on top the measurements total count
        val finalTotalCount = totalCount + 1

        val measurementListItems = convertToBloodSugarHistoryListItems(measurements)
        val data = if (params.requestedStartPosition == 0) {
          listOf(NewBloodSugarButton) + measurementListItems
        } else {
          measurementListItems
        }

        callback.onResult(data, position, finalTotalCount)
      }

      override fun onResult(data: MutableList<BloodSugarMeasurement>, position: Int) {
        // Nothing happens here, source data source results are passed to onResult(data, position, totalCount)
      }
    })
  }

  fun dispose() {
    appDatabase.invalidationTracker.removeObserver(invalidationTracker)
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

        BloodSugarHistoryItem(
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
}

class BloodSugarHistoryListItemDataSourceFactory @AssistedInject constructor(
    private val appDatabase: AppDatabase,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val config: BloodSugarSummaryConfig,
    private val bloodSugarUnitPreference: Preference<BloodSugarUnitPreference>,
    @Named("full_date") private val dateFormatter: DateTimeFormatter,
    @Named("time_for_measurement_history") private val timeFormatter: DateTimeFormatter,
    @Assisted private val source: PositionalDataSource<BloodSugarMeasurement>
) : DataSource.Factory<Int, BloodSugarHistoryListItem>() {

  private val disposable = CompositeDisposable()
  private var dataSource: BloodSugarHistoryListItemDataSource? = null

  @AssistedInject.Factory
  interface Factory {
    fun create(
        source: PositionalDataSource<BloodSugarMeasurement>
    ): BloodSugarHistoryListItemDataSourceFactory
  }

  fun toObservable(config: PagedList.Config, detaches: Observable<Unit>): Observable<PagedList<BloodSugarHistoryListItem>> {
    disposable.add(
        detaches.subscribe {
          dataSource?.dispose()
          dataSource = null
          disposable.clear()
        }
    )
    return toObservable(config)
  }

  override fun create(): DataSource<Int, BloodSugarHistoryListItem> {
    dataSource = BloodSugarHistoryListItemDataSource(appDatabase, utcClock, userClock, dateFormatter, timeFormatter, config.bloodSugarEditableDuration, source, bloodSugarUnitPreference.get())
    return dataSource!!
  }
}
