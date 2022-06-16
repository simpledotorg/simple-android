package org.simple.clinic.home.overdue.search

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class OverdueSearchUpdate : Update<OverdueSearchModel, OverdueSearchEvent, OverdueSearchEffect> {

  override fun update(model: OverdueSearchModel, event: OverdueSearchEvent): Next<OverdueSearchModel, OverdueSearchEffect> {
    return when (event) {
      is OverdueSearchHistoryLoaded -> next(model.overdueSearchHistoryLoaded(event.searchHistory))
    }
  }
}
