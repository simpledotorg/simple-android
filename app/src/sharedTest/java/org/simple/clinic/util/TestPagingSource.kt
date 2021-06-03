package org.simple.clinic.util

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import io.reactivex.Single

class TestPagingSource<K : Any, V : Any>(
    private val items: List<V>
) : RxPagingSource<K, V>() {

  override fun loadSingle(params: LoadParams<K>): Single<LoadResult<K, V>> {
    return Single.just(LoadResult.Page(data = items, prevKey = null, nextKey = null))
  }

  override fun getRefreshKey(state: PagingState<K, V>): K? {
    return null
  }
}
