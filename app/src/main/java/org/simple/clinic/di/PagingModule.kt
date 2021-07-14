package org.simple.clinic.di

import androidx.paging.Config
import androidx.paging.PagedList
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.PagingSize.Page.AllRecentPatients
import org.simple.clinic.di.PagingSize.Page.DrugsSearchResults
import org.simple.clinic.remoteconfig.ConfigReader
import javax.inject.Named

@Module
class PagingModule {

  @Provides
  @Named("for_measurement_history")
  fun providePagingConfigForMeasurementHistory(): PagedList.Config {
    return Config(
        pageSize = 20,
        prefetchDistance = 10,
        initialLoadSizeHint = 40,
        enablePlaceholders = false
    )
  }

  @Provides
  @PagingSize(AllRecentPatients)
  fun providesAllRecentPatientsPageSize(): Int {
    return 25
  }

  @Provides
  @PagingSize(DrugsSearchResults)
  fun providesDrugSearchResultsPageSize(configReader: ConfigReader): Int {
    return configReader.long("custom_drugs_search_results_page_size", 25).toInt()
  }
}
