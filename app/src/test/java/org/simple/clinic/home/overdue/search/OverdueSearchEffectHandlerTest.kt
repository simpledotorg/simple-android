package org.simple.clinic.home.overdue.search

import com.f2prateek.rx.preferences2.Preference
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

  private val overdueSearchHistoryPreference = mock<Preference<Set<String>>>()
  private val overdueSearchConfig = OverdueSearchConfig(minLengthOfSearchQuery = 3)
  private val effectHandler = OverdueSearchEffectHandler(
      overdueSearchHistoryPreference = overdueSearchHistoryPreference,
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

    whenever(overdueSearchHistoryPreference.asObservable()) doReturn Observable.just(searchHistory)

    // when
    effectHandlerTestCase.dispatch(LoadOverdueSearchHistory)

    // then
    effectHandlerTestCase.assertOutgoingEvents(OverdueSearchHistoryLoaded(searchHistory))
  }

  @Test
  fun `when validate overdue search query effect is received, then validate the search query`() {
    // when
    effectHandlerTestCase.dispatch(ValidateOverdueSearchQuery("Babri"))

    // then
    effectHandlerTestCase.assertOutgoingEvents(OverdueSearchQueryValidated(Valid("Babri")))
  }
}
