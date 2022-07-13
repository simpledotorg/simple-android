package org.simple.clinic.home.overdue.search

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
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Test
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Valid
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.download.OverdueDownloadScheduler
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.PagingSourceFactory
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.time.LocalDate
import java.util.UUID

class OverdueSearchEffectHandlerTest {

  private val pagingLoadSize = 15

  private val overdueSearchHistory = mock<OverdueSearchHistory>()
  private val overdueSearchConfig = OverdueSearchConfig(minLengthOfSearchQuery = 3, searchHistoryLimit = 5, pagingLoadSize = pagingLoadSize)
  private val uiActions = mock<OverdueSearchUiActions>()
  private val viewEffectHandler = OverdueSearchViewEffectHandler(uiActions)
  private val appointmentRepository = mock<AppointmentRepository>()
  private val pagerFactory = mock<PagerFactory>()
  private val currentFacility = TestData.facility(uuid = UUID.fromString("94db5d90-d483-4755-892a-97fde5a870fe"))
  private val pagingCacheScope = TestScope()
  private val overdueDownloadScheduler = mock<OverdueDownloadScheduler>()
  private val effectHandler = OverdueSearchEffectHandler(
      overdueSearchHistory = overdueSearchHistory,
      overdueSearchQueryValidator = OverdueSearchQueryValidator(overdueSearchConfig),
      schedulersProvider = TestSchedulersProvider.trampoline(),
      appointmentRepository = appointmentRepository,
      pagerFactory = pagerFactory,
      overdueSearchConfig = overdueSearchConfig,
      currentFacility = { currentFacility },
      overdueDownloadScheduler = overdueDownloadScheduler,
      viewEffectsConsumer = viewEffectHandler::handle,
      pagingCacheScope = pagingCacheScope
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

  @Test
  fun `when search overdue patient effect is received, then search overdue patient`() {
    // given
    val query = "Ani"
    val overdueAppointment = listOf(TestData.overdueAppointment(
        facilityUuid = currentFacility.uuid,
        name = "Anish Acharya",
        patientUuid = UUID.fromString("37259e96-e757-4608-aeae-f1a20b088f09")
    ), TestData.overdueAppointment(
        facilityUuid = currentFacility.uuid,
        name = "Anirban Dar",
        patientUuid = UUID.fromString("53659148-a157-4aa4-92fb-c0a7991ae872")
    ))

    val expectedPagingData = PagingData.from(overdueAppointment)

    whenever(pagerFactory.createPager(
        sourceFactory = any<PagingSourceFactory<Int, OverdueAppointment>>(),
        pageSize = eq(pagingLoadSize),
        enablePlaceholders = eq(false),
        initialKey = eq(null),
        cacheScope = eq(pagingCacheScope)
    )) doReturn Observable.just(expectedPagingData)

    // when
    effectHandlerTestCase.dispatch(SearchOverduePatients(query, LocalDate.of(2022, 3, 22)))

    // then
    effectHandlerTestCase.assertOutgoingEvents(OverdueSearchResultsLoaded(expectedPagingData))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when set overdue search query effect is received, then set overdue search query`() {
    // when
    effectHandlerTestCase.dispatch(SetOverdueSearchQuery("Babri"))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).setOverdueSearchQuery("Babri")
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when schedule download effect is received, then schedule the overdue list download`() {
    // when
    val selectedAppointmentIds = setOf(UUID.fromString("618f0f3f-7ae0-4227-bb26-49ec10ed4ff0"))
    effectHandlerTestCase.dispatch(ScheduleDownload(
        fileFormat = CSV,
        selectedAppointmentIds = selectedAppointmentIds
    ))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verifyZeroInteractions(uiActions)

    verify(overdueDownloadScheduler).schedule(
        CSV,
        selectedAppointmentIds
    )
  }

  @Test
  fun `when open select download format dialog effect is received, then open the dialog`() {
    // when
    effectHandlerTestCase.dispatch(OpenSelectDownloadFormatDialog(emptySet()))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openSelectDownloadFormatDialog(emptySet())
    verifyNoMoreInteractions(uiActions)
  }
}
