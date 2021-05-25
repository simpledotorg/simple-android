package org.simple.clinic.bp

import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import androidx.paging.toObservable
import androidx.room.InvalidationTracker
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.BloodPressureHistoryItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.NewBpButton
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toLocalDateAtZone
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Named

class BloodPressureHistoryListItemDataSource(
    private val appDatabase: AppDatabase,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val dateFormatter: DateTimeFormatter,
    private val timeFormatter: DateTimeFormatter,
    private val canEditFor: Duration,
    private val source: PositionalDataSource<BloodPressureMeasurement>
) : PositionalDataSource<BloodPressureHistoryListItem>() {

  private val invalidationTracker = object : InvalidationTracker.Observer(arrayOf("bloodpressuremeasurement")) {
    override fun onInvalidated(tables: MutableSet<String>) {
      invalidate()
    }
  }

  init {
    appDatabase.invalidationTracker.addObserver(invalidationTracker)
  }

  override fun loadRange(
      params: LoadRangeParams,
      callback: LoadRangeCallback<BloodPressureHistoryListItem>
  ) {
    // we are subtracting 1 from load size and page size to avoid the source data source
    // from using the params because we will be adding a header to the BloodPressureHistoryListItemDataSource list, which
    // is not present in the source data source
    val loadParamsForDatabaseSource = LoadRangeParams(params.startPosition - 1, params.loadSize)
    source.loadRange(loadParamsForDatabaseSource, object : LoadRangeCallback<BloodPressureMeasurement>() {
      override fun onResult(measurements: MutableList<BloodPressureMeasurement>) {
        val measurementListItems = convertToBloodPressureHistoryListItems(measurements)
        val data = if (params.startPosition == 0) {
          listOf(NewBpButton) + measurementListItems
        } else {
          measurementListItems
        }
        callback.onResult(data)
      }
    })
  }

  override fun loadInitial(
      params: LoadInitialParams,
      callback: LoadInitialCallback<BloodPressureHistoryListItem>
  ) {
    // we are subtracting 1 from load size and page size to avoid the source data source
    // from using the params because we will be adding a header to the BloodPressureHistoryListItemDataSource list, which
    // is not present in the source data source
    val loadParamsForDatabaseSource = LoadInitialParams(
        params.requestedStartPosition,
        params.requestedLoadSize - 1,
        params.pageSize - 1,
        params.placeholdersEnabled
    )
    source.loadInitial(loadParamsForDatabaseSource, object : LoadInitialCallback<BloodPressureMeasurement>() {
      override fun onResult(
          measurements: MutableList<BloodPressureMeasurement>,
          position: Int,
          totalCount: Int
      ) {
        // Adding 1 to the total count so that BloodPressureHistoryListItemDataSource knows that we are
        // adding a another item on top the measurements total count
        val finalTotalCount = totalCount + 1

        val measurementListItems = convertToBloodPressureHistoryListItems(measurements)
        val data = if (params.requestedStartPosition == 0) {
          listOf(NewBpButton) + measurementListItems
        } else {
          measurementListItems
        }

        callback.onResult(data, position, finalTotalCount)
      }

      override fun onResult(measurements: MutableList<BloodPressureMeasurement>, position: Int) {
        // Nothing happens here, source data source results are passed to onResult(data, position, totalCount)
      }
    })
  }

  fun dispose() {
    appDatabase.invalidationTracker.removeObserver(invalidationTracker)
  }

  private fun convertToBloodPressureHistoryListItems(measurements: List<BloodPressureMeasurement>): List<BloodPressureHistoryListItem> {
    val measurementsByDate = measurements.groupBy { it.recordedAt.toLocalDateAtZone(userClock.zone) }

    return measurementsByDate.mapValues { (_, measurementsList) ->
      val hasMultipleMeasurementsInSameDate = measurementsList.size > 1
      val now = Instant.now(utcClock)

      measurementsList.map { measurement ->
        val isBpEditable = isBpEditable(now, measurement, canEditFor)
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
}

class BloodPressureHistoryListItemDataSourceFactory @AssistedInject constructor(
    private val appDatabase: AppDatabase,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val config: PatientSummaryConfig,
    @Named("full_date") private val dateFormatter: DateTimeFormatter,
    @Named("time_for_measurement_history") private val timeFormatter: DateTimeFormatter,
    @Assisted private val source: PositionalDataSource<BloodPressureMeasurement>
) : DataSource.Factory<Int, BloodPressureHistoryListItem>() {

  private val disposable = CompositeDisposable()
  private var dataSource: BloodPressureHistoryListItemDataSource? = null

  @AssistedFactory
  interface Factory {
    fun create(
        source: PositionalDataSource<BloodPressureMeasurement>
    ): BloodPressureHistoryListItemDataSourceFactory
  }

  fun toObservable(
      config: PagedList.Config,
      detaches: Observable<Unit>
  ): Observable<PagedList<BloodPressureHistoryListItem>> {
    disposable.add(
        detaches.subscribe {
          dataSource?.dispose()
          dataSource = null
          disposable.clear()
        }
    )
    return toObservable(config)
  }

  override fun create(): DataSource<Int, BloodPressureHistoryListItem> {
    dataSource = BloodPressureHistoryListItemDataSource(appDatabase, utcClock, userClock, dateFormatter, timeFormatter, config.bpEditableDuration, source)
    return dataSource!!
  }
}
