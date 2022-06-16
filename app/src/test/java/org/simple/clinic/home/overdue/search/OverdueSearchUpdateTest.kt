package org.simple.clinic.home.overdue.search

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class OverdueSearchUpdateTest {

  @Test
  fun `when overdue search history is loaded, then update the model`() {
    val defaultModel = OverdueSearchModel.create()
    val searchHistory = setOf(
        "Babri",
        "Ramesh"
    )

    UpdateSpec(OverdueSearchUpdate())
        .given(defaultModel)
        .whenEvent(OverdueSearchHistoryLoaded(searchHistory))
        .then(assertThatNext(
            hasModel(defaultModel.overdueSearchHistoryLoaded(searchHistory)),
            hasNoEffects()
        ))
  }
}
