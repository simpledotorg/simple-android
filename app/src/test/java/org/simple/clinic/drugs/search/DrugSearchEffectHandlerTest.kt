package org.simple.clinic.drugs.search

import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.PagingSourceFactory
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class DrugSearchEffectHandlerTest {

  private val repository = mock<DrugRepository>()
  private val pagerFactory = mock<PagerFactory>()
  private val uiActions = mock<UiActions>()
  private val drugsSearchResultsPageSize = 10

  private val testCase = EffectHandlerTestCase(DrugSearchEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      drugsRepository = repository,
      pagerFactory = pagerFactory,
      drugsSearchResultsPageSize = drugsSearchResultsPageSize,
      uiActions = uiActions
  ).build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when search drugs effect is received, then search drugs`() {
    // given
    val searchQuery = "Amlodip"
    val searchResults = PagingData.from(listOf(
        TestData.drug(id = UUID.fromString("d9af2d06-0c52-430a-9dc4-64aef8d35a5b"),
            name = "Amlodipine",
            dosage = "10 mg"),
        TestData.drug(id = UUID.fromString("ed1062da-05f1-4125-bee5-ee8606620020"),
            name = "Amlodipine",
            dosage = "20 mg")
    ))

    whenever(pagerFactory.createPager(
        sourceFactory = any<PagingSourceFactory<Int, Drug>>(),
        pageSize = eq(drugsSearchResultsPageSize),
        initialKey = eq(null)
    )) doReturn Observable.just(searchResults)

    // when
    testCase.dispatch(SearchDrugs(searchQuery))

    // then
    testCase.assertOutgoingEvents(DrugsSearchResultsLoaded(searchResults))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when set drugs search results effect is received, then set search results`() {
    // given
    val searchResults = PagingData.from(listOf(
        TestData.drug(id = UUID.fromString("d9af2d06-0c52-430a-9dc4-64aef8d35a5b"),
            name = "Amlodipine",
            dosage = "10 mg"),
        TestData.drug(id = UUID.fromString("ed1062da-05f1-4125-bee5-ee8606620020"),
            name = "Amlodipine",
            dosage = "20 mg")
    ))

    // when
    testCase.dispatch(SetDrugsSearchResults(searchResults))

    // then
    verify(uiActions).setDrugSearchResults(searchResults)
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }
}
