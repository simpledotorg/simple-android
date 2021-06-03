package org.simple.clinic.util

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.rxjava2.observable
import io.reactivex.Observable
import javax.inject.Inject

class SimplePagerFactory @Inject constructor() {

  fun <K : Any, V : Any> createPager(
      config: PagingConfig,
      source: PagingSource<K, V>
  ): Observable<PagingData<V>> {
    return Pager(config = config) { source }.observable
  }
}
