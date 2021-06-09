package org.simple.clinic

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams.Refresh
import androidx.paging.PagingSource.LoadResult.Page
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxObservable

class PagingTestCase<K : Any, V : Any>(
    private val pagingSource: PagingSource<K, V>,
    loadSize: Int,
    key: K? = null,
    placeholdersEnabled: Boolean = false
) {

  private val loadParams = Refresh(key, loadSize, placeholdersEnabled)

  val data: Observable<List<V>>
    get() = rxObservable {
      val page = pagingSource.load(loadParams) as Page<K, V>
      send(page.data)
    }
}
