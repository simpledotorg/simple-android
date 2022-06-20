package org.simple.clinic.home.overdue.search

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Valid
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class OverdueSearchEffectHandlerTest {

  private val overdueSearchHistory = mock<OverdueSearchHistory>()
  private val overdueSearchConfig = OverdueSearchConfig(minLengthOfSearchQuery = 3, searchHistoryLimit = 5)
  private val effectHandler = OverdueSearchEffectHandler(
      overdueSearchHistory = overdueSearchHistory,
      overdueSearchQueryValidator = OverdueSearchQueryValidator(overdueSearchConfig),
      schedulersProvider = TestSchedulersProvider.trampoline()
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load search history effect is received, then load the search history`() {
    // given
    val searchHistory = setOf(
        "Babri",
        "Narwar",
        "Ramesh"
    )

    whenever(overdueSearchHistory.fetch()) doReturn Observable.just(searchHistory)

    // when
    effectHandlerTestCase.dispatch(LoadOverdueSearchHistory)

    // then
    effectHandlerTestCase.assertOutgoingEvents(OverdueSearchHistoryLoaded(setOf(
        "Babri",
        "Narwar",
        "Ramesh"
    )))
  }

  @Test
  fun `when validate overdue search query effect is received, then validate the search query`() {
    // when
    effectHandlerTestCase.dispatch(ValidateOverdueSearchQuery("Babri"))

    // then
    effectHandlerTestCase.assertOutgoingEvents(OverdueSearchQueryValidated(Valid("Babri")))
  }

  @Test
  fun `when add to search history effect is received, then add the search query to search history`() {
    // given
    val searchQuery = "Babri"

    // when
    effectHandlerTestCase.dispatch(AddQueryToOverdueSearchHistory(searchQuery))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
  }
}
