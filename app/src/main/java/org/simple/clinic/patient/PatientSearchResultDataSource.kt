package org.simple.clinic.patient

import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import androidx.paging.toObservable
import androidx.room.InvalidationTracker
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.simple.clinic.AppDatabase
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.searchresultsview.SearchResultsItemType

class PatientSearchResultDataSource(
    private val appDatabase: AppDatabase,
    private val currentFacility: Lazy<Facility>,
    private val source: PositionalDataSource<PatientSearchResult>
) : PositionalDataSource<SearchResultsItemType>() {

  private val invalidationTracker = object : InvalidationTracker.Observer(arrayOf("PatientSearchResult")) {
    override fun onInvalidated(tables: MutableSet<String>) {
      invalidate()
    }
  }

  init {
    appDatabase.invalidationTracker.addObserver(invalidationTracker)
  }

  override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<SearchResultsItemType>) {
    val loadParamsForDatabaseSource = LoadInitialParams(
        params.requestedStartPosition,
        params.requestedLoadSize,
        params.pageSize,
        params.placeholdersEnabled
    )

    source.loadInitial(loadParamsForDatabaseSource, object : LoadInitialCallback<PatientSearchResult>() {
      override fun onResult(data: MutableList<PatientSearchResult>, position: Int, totalCount: Int) {

      }

      override fun onResult(data: MutableList<PatientSearchResult>, position: Int) {
        // Nothing happens here, source data source results are passed to onResult(data, position, totalCount)
      }

    })
  }

  override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<SearchResultsItemType>) {
    val loadParamsForDatabaseSource = LoadRangeParams(params.startPosition, params.loadSize)
    source.loadRange(loadParamsForDatabaseSource, object : LoadRangeCallback<PatientSearchResult>() {
      override fun onResult(patientSearchResult: MutableList<PatientSearchResult>) {
      }
    })

  }

  fun dispose() {
    appDatabase.invalidationTracker.removeObserver(invalidationTracker)
  }
}

class PatientSearchDataSourceFactory constructor(
    private val appDatabase: AppDatabase,
    private val currentFacility: Lazy<Facility>,
    private val patientSearchCriteria: PatientSearchCriteria,
    private var config: PatientConfig
) : DataSource.Factory<Int, SearchResultsItemType>() {

  private val disposable = CompositeDisposable()
  private var dataSource: PatientSearchResultDataSource? = null

  fun toObservable(config: PagedList.Config, detaches: Observable<Unit>): Observable<PagedList<SearchResultsItemType>> {
    disposable.add(
        detaches.subscribe {
          dataSource?.dispose()
          dataSource = null
          disposable.clear()
        }
    )
    return toObservable(config)
  }

  override fun create(): DataSource<Int, SearchResultsItemType> {

    dataSource = when (patientSearchCriteria) {
      is Name -> PatientSearchResultDataSource(
          appDatabase,
          currentFacility,
          appDatabase
              .patientSearchDao()
              .searchByNamePaginated(patientSearchCriteria.patientName)
              .create() as PositionalDataSource<PatientSearchResult>
      )
      is PhoneNumber -> PatientSearchResultDataSource(
          appDatabase,
          currentFacility,
          appDatabase
              .patientSearchDao()
              .searchByPhoneNumberPaginated(patientSearchCriteria.phoneNumber)
              .create() as PositionalDataSource<PatientSearchResult>
      )
    }
    return dataSource!!
  }

}
