package org.simple.clinic.home.overdue

import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.analytics.NetworkCapabilitiesProvider
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.OverdueAppointmentSelector
import org.simple.clinic.overdue.callresult.Outcome
import org.simple.clinic.overdue.download.OverdueDownloadScheduler
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.TestUserClock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class OverdueEffectHandlerTest {

  private val facility = TestData.facility(
      uuid = UUID.fromString("251deca2-d219-4863-80fc-e7d48cb22b1b"),
      name = "PHC Obvious",
      facilityConfig = FacilityConfig(
          diabetesManagementEnabled = true,
          teleconsultationEnabled = false,
          monthlyScreeningReportsEnabled = false,
          monthlySuppliesReportsEnabled = false
      )
  )
  private val uiActions = mock<OverdueUiActions>()
  private val pagerFactory = mock<PagerFactory>()
  private val overdueAppointmentsConfig = OverdueAppointmentsConfig(
      overdueAppointmentsLoadSize = 10
  )
  private val networkCapabilitiesProvider = mock<NetworkCapabilitiesProvider>()
  private val overdueDownloadScheduler = mock<OverdueDownloadScheduler>()
  private val viewEffectHandler = OverdueViewEffectHandler(uiActions)
  private val appointmentRepository = mock<AppointmentRepository>()
  private val overdueAppointmentSelector = mock<OverdueAppointmentSelector>()
  private val effectHandler = OverdueEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      appointmentRepository = appointmentRepository,
      currentFacilityStream = Observable.just(facility),
      pagerFactory = pagerFactory,
      overdueAppointmentsConfig = overdueAppointmentsConfig,
      overdueDownloadScheduler = overdueDownloadScheduler,
      userClock = TestUserClock(Instant.parse("2018-01-01T00:00:00Z")),
      overdueAppointmentSelector = overdueAppointmentSelector,
      viewEffectsConsumer = viewEffectHandler::handle
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
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when show no internet connection dialog effect is received, then show no internet connection dialog`() {
    // when
    effectHandlerTestCase.dispatch(ShowNoActiveNetworkConnectionDialog)

    // then
    verify(uiActions).showNoActiveNetworkConnectionDialog()
    effectHandlerTestCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when open select download format effect is received, then open select download format dialog`() {
    // when
    effectHandlerTestCase.dispatch(OpenSelectDownloadFormatDialog)

    // then
    verify(uiActions).openSelectDownloadFormatDialog()
    effectHandlerTestCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when open select share format effect is received, then open select download format dialog`() {
    // when
    effectHandlerTestCase.dispatch(OpenSelectShareFormatDialog)

    // then
    verify(uiActions).openSelectShareFormatDialog()
    effectHandlerTestCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when schedule download effect is received, then schedule the overdue list download`() {
    // when
    val selectedAppointmentIds = setOf(UUID.fromString("618f0f3f-7ae0-4227-bb26-49ec10ed4ff0"))
    effectHandlerTestCase.dispatch(ScheduleDownload(
        fileFormat = CSV
    ))

    // given
    effectHandlerTestCase.assertNoOutgoingEvents()
    verifyNoInteractions(uiActions)

    verify(overdueDownloadScheduler).schedule(
        fileFormat = CSV
    )
  }

  @Test
  fun `when open progress for sharing dialog effect is received, then open progress for sharing dialog`() {
    // when
    effectHandlerTestCase.dispatch(OpenSharingInProgressDialog)

    // then
    verify(uiActions).openProgressForSharingDialog()
    effectHandlerTestCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when load overdue appointments effect is received, then load overdue appointments`() {
    // given
    val effectHandler = OverdueEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        appointmentRepository = appointmentRepository,
        currentFacilityStream = Observable.just(facility),
        pagerFactory = pagerFactory,
        overdueAppointmentsConfig = overdueAppointmentsConfig,
        overdueDownloadScheduler = overdueDownloadScheduler,
        userClock = TestUserClock(Instant.parse("2018-03-01T00:00:00Z")),
        overdueAppointmentSelector = overdueAppointmentSelector,
        viewEffectsConsumer = viewEffectHandler::handle
    ).build()
    val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)


    val pendingAppointmentUuid = UUID.fromString("ba2e8f01-9693-41e8-82f8-c24916c8f5b4")
    val agreedToVisitAppointmentUuid = UUID.fromString("2730cc90-ceaf-4dbf-88ad-9d615dada766")
    val removedAppointmentUuid = UUID.fromString("f2c271b8-c18b-4baf-8f04-dfc6552fc2eb")
    val moreThanAYearAppointmentUuid = UUID.fromString("cb89b308-9118-4281-8bd6-f27fede037ff")

    val pendingAppointment = TestData.overdueAppointment(
        name = "Pending",
        appointment = TestData.appointment(
            uuid = pendingAppointmentUuid,
            facilityUuid = facility.uuid,
            scheduledDate = LocalDate.parse("2018-04-01"),
            createdAt = Instant.parse("2018-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
            deletedAt = null
        )
    )
    val agreedToVisitAppointment = TestData.overdueAppointment(
        name = "Agreed To Visit",
        appointment = TestData.appointment(
            uuid = agreedToVisitAppointmentUuid,
            facilityUuid = facility.uuid,
            scheduledDate = LocalDate.parse("2018-04-01"),
            createdAt = Instant.parse("2018-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
            deletedAt = null
        ),
        callResult = TestData.callResult(
            appointmentId = agreedToVisitAppointmentUuid,
            outcome = Outcome.AgreedToVisit
        )
    )

    val removedAppointment = TestData.overdueAppointment(
        name = "Removed",
        appointment = TestData.appointment(
            uuid = removedAppointmentUuid,
            facilityUuid = facility.uuid,
            scheduledDate = LocalDate.parse("2018-04-01"),
            createdAt = Instant.parse("2018-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
            deletedAt = null
        ),
        callResult = TestData.callResult(
            appointmentId = removedAppointmentUuid,
            removeReason = AppointmentCancelReason.PatientNotResponding,
            outcome = Outcome.RemovedFromOverdueList
        )
    )
    val moreThanAnYearAppointment = TestData.overdueAppointment(
        name = "More Than An Year",
        appointment = TestData.appointment(
            uuid = moreThanAYearAppointmentUuid,
            facilityUuid = facility.uuid,
            scheduledDate = LocalDate.parse("2016-04-01"),
            createdAt = Instant.parse("2016-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2016-01-01T00:00:00Z"),
            deletedAt = null
        ),
        callResult = TestData.callResult(
            appointmentId = moreThanAYearAppointmentUuid,
            removeReason = AppointmentCancelReason.PatientNotResponding,
            outcome = Outcome.RemovedFromOverdueList
        )
    )

    val overdueAppointments = listOf(
        pendingAppointment,
        agreedToVisitAppointment,
        removedAppointment,
        moreThanAnYearAppointment
    )

    whenever(appointmentRepository.overdueAppointmentsInFacilityNew(
        since = LocalDate.parse("2018-04-03"),
        facilityId = facility.uuid
    )) doReturn Observable.just(overdueAppointments)

    // when
    effectHandlerTestCase.dispatch(LoadOverdueAppointments(
        overdueSince = LocalDate.parse("2018-04-03"),
        facility = facility
    ))

    // then
    effectHandlerTestCase.assertOutgoingEvents(OverdueAppointmentsLoaded(
        overdueAppointmentSections = OverdueAppointmentSections(
            pendingAppointments = listOf(pendingAppointment),
            agreedToVisitAppointments = listOf(agreedToVisitAppointment),
            remindToCallLaterAppointments = emptyList(),
            removedFromOverdueAppointments = listOf(removedAppointment),
            moreThanAnYearOverdueAppointments = listOf(moreThanAnYearAppointment)
        )
    ))
    effectHandlerTestCase.dispose()

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when open overdue search view effect is received, then open the overdue search screen`() {
    // when
    effectHandlerTestCase.dispatch(OpenOverdueSearch)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openOverdueSearch()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when toggle overdue appointment selection effect is received, then toggle the overdue appointment selection`() {
    // given
    val appointmentId = UUID.fromString("97667670-26b9-42ed-ab09-f7174c5ade7f")

    // when
    effectHandlerTestCase.dispatch(ToggleOverdueAppointmentSelection(appointmentId))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(overdueAppointmentSelector).toggleSelection(appointmentId)
    verifyNoMoreInteractions(overdueAppointmentSelector)

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when load selected overdue appointments effect is received, then load the selected overdue appointments`() {
    // given
    val selectedAppointmentIds = setOf(
        UUID.fromString("b224924c-81b0-42ba-b876-cb6d8253e47c"),
        UUID.fromString("3af05d37-8c6c-4a03-9b3c-81f3d5152bd0")
    )

    whenever(overdueAppointmentSelector.selectedAppointmentIdsStream) doReturn Observable.just(selectedAppointmentIds)

    // when
    effectHandlerTestCase.dispatch(LoadSelectedOverdueAppointmentIds)

    // then
    effectHandlerTestCase.assertOutgoingEvents(SelectedOverdueAppointmentsLoaded(selectedAppointmentIds))

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when clear selected overdue appointments effect is received, then clear the selected overdue appointments`() {
    // when
    effectHandlerTestCase.dispatch(ClearSelectedOverdueAppointments)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(overdueAppointmentSelector).clearSelection()
    verifyNoMoreInteractions(overdueAppointmentSelector)

    verifyNoInteractions(uiActions)
  }
}
