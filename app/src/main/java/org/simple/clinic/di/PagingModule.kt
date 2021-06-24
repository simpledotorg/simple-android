package org.simple.clinic.di

import androidx.paging.Config
import androidx.paging.PagedList
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.PagingSize.Page.AllRecentPatients
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
}
