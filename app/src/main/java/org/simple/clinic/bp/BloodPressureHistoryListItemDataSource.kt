package org.simple.clinic.bp

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Duration
import org.threeten.bp.format.DateTimeFormatter
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
  override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<BloodPressureHistoryListItem>) {

  }

  override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<BloodPressureHistoryListItem>) {

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

  @AssistedInject.Factory
  interface Factory {
    fun create(
        source: PositionalDataSource<BloodPressureMeasurement>
    ): BloodPressureHistoryListItemDataSourceFactory
  }

  override fun create(): DataSource<Int, BloodPressureHistoryListItem> {
    return BloodPressureHistoryListItemDataSource(appDatabase, utcClock, userClock, dateFormatter, timeFormatter, config.bpEditableDuration, source)
  }
}
