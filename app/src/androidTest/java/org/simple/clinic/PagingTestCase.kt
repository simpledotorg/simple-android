package org.simple.clinic

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams.Refresh
import androidx.paging.PagingSource.LoadResult.Page
import kotlinx.coroutines.runBlocking

class PagingTestCase<K : Any, V : Any>(
    private val pagingSource: PagingSource<K, V>,
    loadSize: Int,
    key: K? = null,
    placeholdersEnabled: Boolean = false
) {

  private val loadParams = Refresh(key, loadSize, placeholdersEnabled)

  fun loadPage(): Page<K, V> {
    return runBlocking { pagingSource.load(loadParams) as Page<K, V> }
  }
}
