package org.simple.clinic.home.overdue.search

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase

class OverdueSearchEffectHandlerTest {

  @Test
  fun `when load search history effect is received, then load the search history`() {
    // given
    val overdueSearchHistoryPreference = mock<Preference<Set<String>>>()
    val effectHandler = OverdueSearchEffectHandler(
        overdueSearchHistoryPreference = overdueSearchHistoryPreference
    ).build()
    val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)
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
    effectHandlerTestCase.dispose()
  }
}
