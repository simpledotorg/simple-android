package org.simple.clinic.home.overdue.search

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Empty
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.LengthTooShort
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Valid
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class OverdueSearchUpdate : Update<OverdueSearchModel, OverdueSearchEvent, OverdueSearchEffect> {

  override fun update(model: OverdueSearchModel, event: OverdueSearchEvent): Next<OverdueSearchModel, OverdueSearchEffect> {
    return when (event) {
      is OverdueSearchHistoryLoaded -> next(model.overdueSearchHistoryLoaded(event.searchHistory))
      is OverdueSearchQueryChanged -> next(
          model.overdueSearchQueryChanged(event.searchQuery),
          ValidateOverdueSearchQuery(event.searchQuery)
      )
      is OverdueSearchQueryValidated -> searchQueryValidated(event)
    }
  }

  private fun searchQueryValidated(event: OverdueSearchQueryValidated): Next<OverdueSearchModel, OverdueSearchEffect> {
    return when (val result = event.result) {
      // TODO: Start overdue search
      is Valid -> dispatch(AddQueryToOverdueSearchHistory(result.searchQuery))
      Empty,
      LengthTooShort -> noChange()
    }
  }
}
