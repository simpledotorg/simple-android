package org.simple.clinic

import androidx.paging.PagingSource.LoadParams.Refresh
import androidx.paging.PagingSource.LoadResult.Page
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxObservable
import org.simple.clinic.util.PagingSourceFactory

class PagingTestCase<K : Any, V : Any>(
    private val pagingSource: PagingSourceFactory<K, V>,
    loadSize: Int,
    key: K? = null,
    placeholdersEnabled: Boolean = false
) {

  private val loadParams = Refresh(key, loadSize, placeholdersEnabled)

  val data: Observable<List<V>>
    get() = rxObservable {
      val page = pagingSource.invoke().load(loadParams) as Page<K, V>
      send(page.data)
    }
}
