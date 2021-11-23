package org.simple.clinic.home.overdue

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
import org.simple.clinic.analytics.NetworkCapabilitiesProvider
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.PagingSourceFactory
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.LocalDate
import java.util.UUID

class OverdueEffectHandlerTest {

  private val facility = TestData.facility(
      uuid = UUID.fromString("251deca2-d219-4863-80fc-e7d48cb22b1b"),
      name = "PHC Obvious",
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = true,
          teleconsultationEnabled = false
      )
  )
  private val uiActions = mock<OverdueUiActions>()
  private val pagerFactory = mock<PagerFactory>()
  private val overdueAppointmentsConfig = OverdueAppointmentsConfig(
      overdueAppointmentsLoadSize = 10
  )
  private val networkCapabilitiesProvider = mock<NetworkCapabilitiesProvider>()
  private val effectHandler = OverdueEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      appointmentRepository = mock(),
      currentFacilityStream = Observable.just(facility),
      pagerFactory = pagerFactory,
      overdueAppointmentsConfig = overdueAppointmentsConfig,
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when open patient summary effect is received, then open patient summary`() {
    // given
    val patientUuid = UUID.fromString("e6794bf5-447e-4588-8df2-5e2a07d23bc4")

    // when
    effectHandlerTestCase.dispatch(OpenPatientSummary(patientUuid))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientSummary(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the load current facility effect is received, then the current facility must be loaded`() {
    // when
    effectHandlerTestCase.dispatch(LoadCurrentFacility)

    // then
    effectHandlerTestCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when show overdue appointments effect is received, then show overdue appointments`() {
    // given
    val overdueAppointments = listOf(
        TestData.overdueAppointment(
            appointmentUuid = UUID.fromString("e960f0dd-e575-4a1d-b8c1-6676097b4b54")
        ),
        TestData.overdueAppointment(
            appointmentUuid = UUID.fromString("65c380ad-d2e4-49f5-a348-07e8d489dab1")
        )
    )
    val pagingData = PagingData.from(overdueAppointments)

    // when
    effectHandlerTestCase.dispatch(ShowOverdueAppointments(overdueAppointments = pagingData,
        isDiabetesManagementEnabled = true))

    // then
    verify(uiActions).showOverdueAppointments(overdueAppointments = pagingData,
        isDiabetesManagementEnabled = true)
  }

  @Test
  fun `when load overdue appointments effect is received, then load overdue appointments with patients with no phone numbers`() {
    // given
    val overdueAppointments = PagingData.from(listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("94295f1e-9087-427e-be9d-552ab0581443")),
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("3379cdf4-9693-4ad8-b0d6-1006f6dd48ff"))
    ))

    whenever(pagerFactory.createPager(
        sourceFactory = any<PagingSourceFactory<Int, OverdueAppointment>>(),
        pageSize = eq(overdueAppointmentsConfig.overdueAppointmentsLoadSize),
        enablePlaceholders = eq(true),
        initialKey = eq(null)
    )) doReturn Observable.just(overdueAppointments)

    // when
    effectHandlerTestCase.dispatch(LoadOverdueAppointments(overdueSince = LocalDate.parse("2018-01-01"), facility = facility))

    // then
    verifyZeroInteractions(uiActions)

    effectHandlerTestCase.assertOutgoingEvents(OverdueAppointmentsLoaded(overdueAppointments))
  }

  @Test
  fun `when show no internet connection dialog effect is received, then show no internet connection dialog`() {
    // when
    effectHandlerTestCase.dispatch(ShowNoActiveNetworkConnectionDialog)

    // then
    verify(uiActions).showNoActiveNetworkConnectionDialog()
    effectHandlerTestCase.assertNoOutgoingEvents()
  }
}
