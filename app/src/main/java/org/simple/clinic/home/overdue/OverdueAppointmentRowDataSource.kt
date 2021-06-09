package org.simple.clinic.home.overdue

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
import org.simple.clinic.facility.Facility
import org.simple.clinic.util.UserClock
import java.time.format.DateTimeFormatter
import javax.inject.Named

class OverdueAppointmentRowDataSource(
    private val appDatabase: AppDatabase,
    private val userClock: UserClock,
    private val dateFormatter: DateTimeFormatter,
    private val currentFacility: Facility,
    private val source: PositionalDataSource<OverdueAppointment>
) : PositionalDataSource<OverdueAppointmentRow>() {

  private val invalidationObserver = object : InvalidationTracker.Observer("OverdueAppointment") {
    override fun onInvalidated(tables: MutableSet<String>) {
      invalidate()
    }
  }

  init {
    appDatabase.invalidationTracker.addObserver(invalidationObserver)
  }

  override fun loadRange(
      params: LoadRangeParams,
      callback: LoadRangeCallback<OverdueAppointmentRow>
  ) {
    source.loadRange(params, object : LoadRangeCallback<OverdueAppointment>() {
      override fun onResult(data: List<OverdueAppointment>) {
        val listItems = OverdueAppointmentRow.from(
            appointments = data,
            clock = userClock,
            dateFormatter = dateFormatter,
            isDiabetesManagementEnabled = currentFacility.config.diabetesManagementEnabled
        )

        callback.onResult(listItems)
      }
    })
  }

  override fun loadInitial(
      params: LoadInitialParams,
      callback: LoadInitialCallback<OverdueAppointmentRow>
  ) {
    source.loadInitial(params, object : LoadInitialCallback<OverdueAppointment>() {
      override fun onResult(data: List<OverdueAppointment>, position: Int, totalCount: Int) {
        val listItems = OverdueAppointmentRow.from(
            appointments = data,
            clock = userClock,
            dateFormatter = dateFormatter,
            isDiabetesManagementEnabled = currentFacility.config.diabetesManagementEnabled
        )

        callback.onResult(listItems, position, totalCount)
      }

      override fun onResult(data: List<OverdueAppointment>, position: Int) {
        // Nothing happens here, source data source results are passed to onResult(data, position, totalCount)
      }
    })
  }

  fun dispose() {
    appDatabase.invalidationTracker.removeObserver(invalidationObserver)
  }

  class Factory @AssistedInject constructor(
      private val appDatabase: AppDatabase,
      private val userClock: UserClock,
      @Named("full_date") private val dateFormatter: DateTimeFormatter,
      @Assisted private val currentFacility: Facility,
      @Assisted private val source: PositionalDataSource<OverdueAppointment>
  ) : DataSource.Factory<Int, OverdueAppointmentRow>() {

    @AssistedFactory
    interface InjectionFactory {
      fun create(
          currentFacility: Facility,
          source: PositionalDataSource<OverdueAppointment>
      ): Factory
    }

    private val subscriptions = CompositeDisposable()
    private var dataSource: OverdueAppointmentRowDataSource? = null

    fun toObservable(
        config: PagedList.Config,
        detaches: Observable<Unit>
    ): Observable<PagedList<OverdueAppointmentRow>> {
      val subscription = detaches.subscribe {
        dataSource?.dispose()
        dataSource = null
        subscriptions.clear()
      }

      subscriptions.add(subscription)

      return toObservable(config)
    }

    override fun create(): DataSource<Int, OverdueAppointmentRow> {
      dataSource = OverdueAppointmentRowDataSource(
          appDatabase = appDatabase,
          userClock = userClock,
          dateFormatter = dateFormatter,
          currentFacility = currentFacility,
          source = source
      )

      return dataSource!!
    }
  }
}
