package org.simple.clinic.drugs.search

import androidx.paging.PagingData
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.PagingSourceFactory
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.UUID

class DrugSearchEffectHandlerTest {

  private val repository = mock<DrugRepository>()
  private val pagerFactory = mock<PagerFactory>()
  private val uiActions = mock<UiActions>()
  private val drugsSearchResultsPageSize = 10
  private val facility = TestData.facility(uuid = UUID.fromString("6718d30a-a755-4422-b0a1-802c989c56ce"),
      name = "PHC Obvious")

  private val testCase = EffectHandlerTestCase(DrugSearchEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      drugsRepository = repository,
      pagerFactory = pagerFactory,
      drugsSearchResultsPageSize = drugsSearchResultsPageSize,
      currentFacility = { facility },
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
        enablePlaceholders = eq(false),
        initialKey = eq(null),
        cacheScope = eq(null)
    )) doReturn Observable.just(searchResults)

    // when
    testCase.dispatch(SearchDrugs(searchQuery))

    // then
    testCase.assertOutgoingEvents(DrugsSearchResultsLoaded(searchResults))

    verifyNoInteractions(uiActions)
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

  @Test
  fun `when open custom drug entry screen from drug list effect is received, then open custom drug entry screen from drug list`() {
    // given
    val drugId = UUID.fromString("05ca8019-0514-4c6b-aa8e-657701229cd5")
    val patientId = UUID.fromString("3d4105bb-8447-42ab-b769-2da2dcd4ba22")

    // when
    testCase.dispatch(OpenCustomDrugEntrySheetFromDrugList(drugId, patientId))

    // then
    verify(uiActions).openCustomDrugEntrySheetFromDrugList(drugId, patientId)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when open custom drug entry screen from drug name effect is received, then open custom drug entry screen from drug name`() {
    // given
    val drugName = "Amlodipine"
    val patientId = UUID.fromString("3d4105bb-8447-42ab-b769-2da2dcd4ba22")

    // when
    testCase.dispatch(OpenCustomDrugEntrySheetFromDrugName(drugName, patientId))

    // then
    verify(uiActions).openCustomDrugEntrySheetFromDrugName(drugName, patientId)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }
}
