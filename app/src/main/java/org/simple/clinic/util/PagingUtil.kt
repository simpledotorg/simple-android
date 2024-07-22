package org.simple.clinic.util

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState

/**
 * Returns the key for [PagingSource] for a non-initial REFRESH load.
 *
 * To prevent a negative key, key is clipped to 0 when the number of items available before
 * anchorPosition is less than the requested amount of initialLoadSize / 2.
 *
 * copied from AndroidX Paging LimitOffsetPagingSource implementation
 */
fun <Value : Any> PagingState<Int, Value>.getClippedRefreshKey(): Int? {
  return when (val anchorPosition = anchorPosition) {
    null -> null
    /**
     *  It is unknown whether anchorPosition represents the item at the top of the screen or item at
     *  the bottom of the screen. To ensure the number of items loaded is enough to fill up the
     *  screen, half of loadSize is loaded before the anchorPosition and the other half is
     *  loaded after the anchorPosition -- anchorPosition becomes the middle item.
     */
    else -> maxOf(0, anchorPosition - (config.initialLoadSize / 2))
  }
}

/**
 * A [LoadResult] that can be returned to trigger a new generation of PagingSource
 *
 * Any loaded data or queued loads prior to returning INVALID will be discarded
 *
 * copied from AndroidX Paging LimitOffsetPagingSource implementation
 */
val INVALID = LoadResult.Invalid<Any, Any>()
