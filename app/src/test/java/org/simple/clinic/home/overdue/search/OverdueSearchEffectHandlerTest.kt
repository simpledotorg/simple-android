package org.simple.clinic.home.overdue.search

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Valid
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class OverdueSearchEffectHandlerTest {

  private val overdueSearchHistory = mock<OverdueSearchHistory>()
  private val overdueSearchConfig = OverdueSearchConfig(minLengthOfSearchQuery = 3, searchHistoryLimit = 5)
  private val uiActions = mock<OverdueSearchUiActions>()
  private val viewEffectHandler = OverdueSearchViewEffectHandler(uiActions)
  private val effectHandler = OverdueSearchEffectHandler(
      overdueSearchHistory = overdueSearchHistory,
      overdueSearchQueryValidator = OverdueSearchQueryValidator(overdueSearchConfig),
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = viewEffectHandler::handle
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

  @Test
  fun `when open patient summary view effect is received, then open patient summary screen`() {
    // given
    val patientUuid = UUID.fromString("fc831110-5da8-4bea-9036-5b5d6334dc1a")

    // when
    effectHandlerTestCase.dispatch(OpenPatientSummary(patientUuid))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientSummaryScreen(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when call overdue patient view effect is received, then open contact patient sheet`() {
    // given
    val patientUuid = UUID.fromString("fc831110-5da8-4bea-9036-5b5d6334dc1a")

    // when
    effectHandlerTestCase.dispatch(OpenContactPatientSheet(patientUuid))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openContactPatientSheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }
}
