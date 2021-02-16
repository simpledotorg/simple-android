package org.simple.clinic.home.overdue

import androidx.paging.PositionalDataSource
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
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

  private val uiActions = mock<OverdueUiActions>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val repository = mock<AppointmentRepository>()
  private val overdueAppointmentDataSource = mock<PositionalDataSource<OverdueAppointment>>()
  private val overdueAppointmentRowDataSourceFactory = mock<OverdueAppointmentRowDataSource.Factory>()
  private val overdueAppointmentRowDataSourceFactoryInjectionFactory = mock<OverdueAppointmentRowDataSource.Factory.InjectionFactory>()
  private val facility = TestData.facility(uuid = UUID.fromString("f4430584-eeaf-4352-b1f5-c21cc96faa6c"))
  private val dateOnClock = LocalDate.parse("2018-01-01")

  private lateinit var testFixture: MobiusTestFixture<OverdueModel, OverdueEvent, OverdueEffect>

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
    verify(uiActions).showOverdueAppointments(overdueAppointmentRowDataSourceFactory)
    verify(uiActions).openPhoneMaskBottomSheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when screen is created then the overdue appointments must be displayed`() {
    // when
    setupController()

    // then
    verify(uiActions).showOverdueAppointments(overdueAppointmentRowDataSourceFactory)
    verifyNoMoreInteractions(uiActions)
  }

  private fun setupController() {
    whenever(repository.overdueAppointmentsDataSource(dateOnClock, facility)).thenReturn(overdueAppointmentDataSource)
    whenever(overdueAppointmentRowDataSourceFactoryInjectionFactory.create(facility, overdueAppointmentDataSource))
        .thenReturn(overdueAppointmentRowDataSourceFactory)

    val effectHandler = OverdueEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        appointmentRepository = repository,
        currentFacility = { facility },
        dataSourceFactory = overdueAppointmentRowDataSourceFactoryInjectionFactory,
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
