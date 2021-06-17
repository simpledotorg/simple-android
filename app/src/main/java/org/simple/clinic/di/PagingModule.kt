package org.simple.clinic.di

import androidx.paging.Config
import androidx.paging.PagedList
import dagger.Module
import dagger.Provides
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
}
