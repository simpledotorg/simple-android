package org.simple.clinic.bloodsugar

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.simple.clinic.AppDatabase
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfig
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Duration
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Named

class BloodSugarHistoryListItemDataSource(
    private val appDatabase: AppDatabase,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val dateFormatter: DateTimeFormatter,
    private val timeFormatter: DateTimeFormatter,
    private val canEditFor: Duration,
    private val source: PositionalDataSource<BloodSugarMeasurement>
) : PositionalDataSource<BloodSugarHistoryListItem>() {

  override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<BloodSugarHistoryListItem>) {

  }

  override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<BloodSugarHistoryListItem>) {

  }
}

class BloodSugarHistoryListItemDataSourceFactory @AssistedInject constructor(
    private val appDatabase: AppDatabase,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val config: BloodSugarSummaryConfig,
    @Named("full_date") private val dateFormatter: DateTimeFormatter,
    @Named("time_for_measurement_history") private val timeFormatter: DateTimeFormatter,
    @Assisted private val source: PositionalDataSource<BloodSugarMeasurement>
) : DataSource.Factory<Int, BloodSugarHistoryListItem>() {

  private var dataSource: BloodSugarHistoryListItemDataSource? = null

  @AssistedInject.Factory
  interface Factory {
    fun create(
        source: PositionalDataSource<BloodSugarMeasurement>
    ): BloodSugarHistoryListItemDataSourceFactory
  }

  override fun create(): DataSource<Int, BloodSugarHistoryListItem> {
    dataSource = BloodSugarHistoryListItemDataSource(appDatabase, utcClock, userClock, dateFormatter, timeFormatter, config.bloodSugarEditableDuration, source)
    return dataSource!!
  }
}
