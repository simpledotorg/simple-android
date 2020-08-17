package org.simple.clinic.searchresultsview

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class SearchResultsInit : Init<SearchResultsModel, SearchResultsEffect> {

  override fun init(model: SearchResultsModel): First<SearchResultsModel, SearchResultsEffect> {
    return first(model)
  }
}
