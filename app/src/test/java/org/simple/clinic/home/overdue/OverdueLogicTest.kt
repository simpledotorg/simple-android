package org.simple.clinic.home.overdue

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.LocalDate
import java.util.UUID

class OverdueLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<OverdueUi>()
  private val uiActions = mock<OverdueUiActions>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val repository = mock<AppointmentRepository>()
  private val facility = TestData.facility(uuid = UUID.fromString("f4430584-eeaf-4352-b1f5-c21cc96faa6c"))
  private val dateOnClock = LocalDate.parse("2018-01-01")

  private lateinit var testFixture: MobiusTestFixture<OverdueModel, OverdueEvent, OverdueEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when screen is created, and overdue list is empty, show empty list UI`() {
    // given
    whenever(repository.overdueAppointments(dateOnClock, facility))
        .thenReturn(Observable.just(emptyList()))

    // when
    setupController()

    // then
    verify(ui).updateList(emptyList(), false)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when showPhoneMaskBottomSheet config is true and call patient is clicked then open phone mask bottom sheet`() {
    // given
    val patientUuid = UUID.fromString("55daf914-82df-4c41-ba1b-131216fed30c")
    val overdueAppointments = listOf(
        TestData.overdueAppointment(
            patientUuid = patientUuid,
            appointmentUuid = UUID.fromString("5ee43e06-44fd-413e-a222-23c79f0b0f3a"),
            phoneNumberUuid = UUID.fromString("1f8c184c-09b2-408c-a87e-96a144b7cf22"),
            facilityUuid = facility.uuid,
            isHighRisk = true
        )
    )
    whenever(repository.overdueAppointments(dateOnClock, facility))
        .thenReturn(Observable.just(overdueAppointments))

    // when
    setupController()
    uiEvents.onNext(CallPatientClicked(patientUuid))

    // then
    verify(ui).updateList(overdueAppointments, false)
    verify(uiActions).openPhoneMaskBottomSheet(patientUuid)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when screen is created then the overdue appointments must be displayed`() {
    // given
    val overdueAppointments = listOf(
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("d79923ea-bedd-484a-ae28-465be3051f45"),
            appointmentUuid = UUID.fromString("5ee43e06-44fd-413e-a222-23c79f0b0f3a"),
            phoneNumberUuid = UUID.fromString("1f8c184c-09b2-408c-a87e-96a144b7cf22"),
            facilityUuid = facility.uuid,
            isHighRisk = true
        ),
        TestData.overdueAppointment(
            patientUuid = UUID.fromString("d89a1040-e732-4dd9-b975-eea7b9c53414"),
            appointmentUuid = UUID.fromString("3e4a7da3-1921-4260-93bd-ff9f1dc98476"),
            phoneNumberUuid = UUID.fromString("50c1fb5c-0406-4e98-8150-696accdab113"),
            facilityUuid = facility.uuid,
            isHighRisk = false
        )
    )
    whenever(repository.overdueAppointments(dateOnClock, facility))
        .thenReturn(Observable.just(overdueAppointments))

    // when
    setupController()

    // then
    verify(ui).updateList(overdueAppointments, false)
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController() {
    val effectHandler = OverdueEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        appointmentRepository = repository,
        currentFacility = Lazy { facility },
        uiActions = uiActions
    )
    val uiRenderer = OverdueUiRenderer(ui)
    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = OverdueModel.create(),
        update = OverdueUpdate(dateOnClock),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render,
        init = OverdueInit(dateOnClock)
    )
    testFixture.start()
  }
}
