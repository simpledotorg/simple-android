package org.simple.clinic.searchresultsview

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class SearchResultsUpdate: Update<SearchResultsModel, SearchResultsEvent, SearchResultsEffect> {

  override fun update(model: SearchResultsModel, event: SearchResultsEvent): Next<SearchResultsModel, SearchResultsEffect> {
    return noChange()
  }
}
