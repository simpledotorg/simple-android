package org.simple.clinic.home.overdue

import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.analytics.NetworkCapabilitiesProvider
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.PagingSourceFactory
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.LocalDate
import java.util.UUID

class OverdueLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiActions = mock<OverdueUiActions>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val repository = mock<AppointmentRepository>()
  private val pagerFactory = mock<PagerFactory>()
  private val networkCapabilitiesProvider = mock<NetworkCapabilitiesProvider>()
  private val facility = TestData.facility(
      uuid = UUID.fromString("f4430584-eeaf-4352-b1f5-c21cc96faa6c"),
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = true,
          teleconsultationEnabled = false
      )
  )
  private val dateOnClock = LocalDate.parse("2018-01-01")

  private lateinit var testFixture: MobiusTestFixture<OverdueModel, OverdueEvent, OverdueEffect>

  private val overdueAppointments = PagingData.from(listOf(
      TestData.overdueAppointment(appointmentUuid = UUID.fromString("829ca241-2266-47d1-be48-0952dd9b2cab")),
      TestData.overdueAppointment(appointmentUuid = UUID.fromString("2cb1d2cd-b6e3-40dd-b2eb-4b690925c123"))
  ))

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when showPhoneMaskBottomSheet config is true and call patient is clicked then open phone mask bottom sheet`() {
    // given
    val patientUuid = UUID.fromString("55daf914-82df-4c41-ba1b-131216fed30c")

    // when
    setupController()
    uiEvents.onNext(CallPatientClicked(patientUuid))

    // then
    verify(uiActions).showOverdueAppointments(overdueAppointments, isDiabetesManagementEnabled = true)
    verify(uiActions).openPhoneMaskBottomSheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when screen is created then the overdue appointments must be displayed`() {
    // when
    setupController()

    // then
    verify(uiActions).showOverdueAppointments(overdueAppointments, isDiabetesManagementEnabled = true)
    verifyNoMoreInteractions(uiActions)
  }

  private fun setupController() {
    whenever(pagerFactory.createPager(
        sourceFactory = any<PagingSourceFactory<Int, OverdueAppointment>>(),
        pageSize = eq(10),
        enablePlaceholders = eq(true),
        initialKey = eq(null)
    )) doReturn Observable.just(overdueAppointments)

    val effectHandler = OverdueEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        appointmentRepository = repository,
        currentFacilityStream = Observable.just(facility),
        pagerFactory = pagerFactory,
        overdueAppointmentsConfig = OverdueAppointmentsConfig(
            overdueAppointmentsLoadSize = 10
        ),
        uiActions = uiActions
    )
    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = OverdueModel.create(),
        update = OverdueUpdate(dateOnClock),
        effectHandler = effectHandler.build(),
        modelUpdateListener = { /* Nothing to do here */ },
        init = OverdueInit()
    )
    testFixture.start()
  }
}
