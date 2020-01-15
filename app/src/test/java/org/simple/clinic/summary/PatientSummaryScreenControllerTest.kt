package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.PatientSummaryScreenControllerTest.GoBackToScreen.HOME
import org.simple.clinic.summary.PatientSummaryScreenControllerTest.GoBackToScreen.PREVIOUS
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSummaryScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<PatientSummaryScreenUi>()
  private val patientRepository = mock<PatientRepository>()
  private val bpRepository = mock<BloodPressureRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientUuid = UUID.fromString("d2fe1916-b76a-4bb6-b7e5-e107f00c3163")
  private val missingPhoneReminderRepository = mock<MissingPhoneReminderRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val reporter = MockAnalyticsReporter()
  private val viewRenderer = PatientSummaryViewRenderer(ui)

  private lateinit var controllerSubscription: Disposable
  private lateinit var testFixture: MobiusTestFixture<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect>

  @Before
  fun setUp() {
    whenever(patientRepository.patient(patientUuid)).doReturn(Observable.never())
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.never())
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).doReturn(Observable.never())
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.never())
    whenever(patientRepository.bpPassportForPatient(patientUuid)).doReturn(Observable.never())

    Analytics.addReporter(reporter)

    val effectHandler = PatientSummaryEffectHandler(TrampolineSchedulersProvider(), patientRepository)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = PatientSummaryModel.from(patientUuid),
        init = PatientSummaryInit(),
        update = PatientSummaryUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = viewRenderer::render
    )
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
    reporter.clear()
    if(::controllerSubscription.isInitialized) {
      controllerSubscription.dispose()
    }
    testFixture.dispose()
  }

  @Test
  @Parameters(method = "params for patient summary populating profile")
  fun `patient's profile should be populated`(intention: OpenIntention, bpPassport: BusinessId?) {
    val addressUuid = UUID.fromString("471253db-11d7-42ae-9e92-1415abd7a418")
    val patient = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid)
    val address = PatientMocker.address(uuid = addressUuid)
    val phoneNumber = None
    val optionalBpPassport = bpPassport.toOptional()

    whenever(patientRepository.patient(patientUuid)).doReturn(Observable.just<Optional<Patient>>(Just(patient)))
    whenever(patientRepository.address(addressUuid)).doReturn(Observable.just<Optional<PatientAddress>>(Just(address)))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(phoneNumber))
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, 100)).doReturn(Observable.never())
    whenever(patientRepository.bpPassportForPatient(patientUuid)).doReturn(Observable.just(optionalBpPassport))

    startMobiusLoop()

    val expectedSummaryProfile = PatientSummaryProfile(patient, address, phoneNumber.toNullable(), optionalBpPassport.toNullable())
    verify(ui).populatePatientProfile(expectedSummaryProfile)
    verify(ui).showEditButton()
  }

  @Suppress("Unused")
  private fun `params for patient summary populating profile`() = listOf(
      listOf(OpenIntention.ViewExistingPatient, PatientMocker.businessId(patientUuid = patientUuid, identifier = Identifier("bp-pass", BpPassport))),
      listOf(OpenIntention.ViewExistingPatient, null),
      listOf(OpenIntention.ViewNewPatient, PatientMocker.businessId(patientUuid = patientUuid)),
      listOf(OpenIntention.ViewNewPatient, null),
      listOf(OpenIntention.LinkIdWithPatient(Identifier("06293b71-0f56-45dc-845e-c05ee4d74153", BpPassport)), PatientMocker.businessId(patientUuid = patientUuid)),
      listOf(OpenIntention.LinkIdWithPatient(Identifier("06293b71-0f56-45dc-845e-c05ee4d74153", BpPassport)), null)
  )

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `when the screen is opened, the viewed patient analytics event must be sent`(openIntention: OpenIntention) {
    setupController(openIntention)

    val expectedEvent = MockAnalyticsReporter.Event("ViewedPatient", mapOf(
        "patientId" to patientUuid.toString(),
        "from" to openIntention.analyticsName()
    ))
    assertThat(reporter.receivedEvents).contains(expectedEvent)
  }

  @Test
  @Parameters(method = "appointment cancelation reasons")
  fun `when patient's phone was marked as invalid after the phone number was last updated then update phone dialog should be shown`(
      openIntention: OpenIntention,
      cancelReason: AppointmentCancelReason
  ) {
    val canceledAppointment = PatientMocker.appointment(status = Cancelled, cancelReason = cancelReason)
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid))
        .doReturn(Observable.just<Optional<Appointment>>(Just(canceledAppointment)))

    val phoneNumber = PatientMocker.phoneNumber(
        patientUuid = patientUuid,
        updatedAt = canceledAppointment.updatedAt - Duration.ofHours(2))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(Just(phoneNumber)))

    setupController(openIntention)

    if (cancelReason == InvalidPhoneNumber) {
      verify(ui).showUpdatePhoneDialog(patientUuid)
    } else {
      verify(ui, never()).showUpdatePhoneDialog(patientUuid)
    }
  }

  @Test
  @Parameters(method = "appointment cancelation reasons")
  fun `when patient's phone was marked as invalid before the phone number was last updated then update phone dialog should not be shown`(
      openIntention: OpenIntention,
      cancelReason: AppointmentCancelReason
  ) {
    val canceledAppointment = PatientMocker.appointment(status = Cancelled, cancelReason = cancelReason)
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid))
        .doReturn(Observable.just<Optional<Appointment>>(Just(canceledAppointment)))

    val phoneNumber = PatientMocker.phoneNumber(
        patientUuid = patientUuid,
        updatedAt = canceledAppointment.updatedAt + Duration.ofHours(2))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(Just(phoneNumber)))

    setupController(openIntention)

    verify(ui, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Test
  @Parameters(method = "appointment cancelation reasons")
  fun `when update phone dialog feature is disabled then it should never be shown`(
      openIntention: OpenIntention,
      cancelReason: AppointmentCancelReason
  ) {
    val appointmentStream = Observable.just(
        None,
        Just(PatientMocker.appointment(cancelReason = null)),
        Just(PatientMocker.appointment(cancelReason = cancelReason)))
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).doReturn(appointmentStream)

    setupController(openIntention)

    verify(ui, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Suppress("unused")
  fun `appointment cancelation reasons`() =
      AppointmentCancelReason
          .values()
          .map { listOf(randomPatientSummaryOpenIntention(), it) }

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `when a canceled appointment with the patient does not exist then update phone dialog should not be shown`(
      openIntention: OpenIntention
  ) {
    val appointmentStream = Observable.just(
        None,
        Just(PatientMocker.appointment(status = Scheduled, cancelReason = null)))
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).doReturn(appointmentStream)

    setupController(openIntention)

    verify(ui, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Test
  fun `when a new patient is missing a phone number, then avoid showing update phone dialog`() {
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).doReturn(Observable.just<Optional<Appointment>>(None))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(None))

    setupController(OpenIntention.ViewNewPatient)

    verify(ui, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Test
  @Parameters(method = "patient summary open intentions except new patient")
  fun `when an existing patient is missing a phone number, a BP is recorded, and the user has never been reminded, then add phone dialog should be shown`(
      openIntention: OpenIntention
  ) {
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(None))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.just(false))
    whenever(missingPhoneReminderRepository.markReminderAsShownFor(patientUuid)).doReturn(Completable.complete())

    setupController(openIntention)
    uiEvents.onNext(PatientSummaryBloodPressureSaved)

    verify(ui).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository).markReminderAsShownFor(patientUuid)
  }

  @Test
  @Parameters(method = "patient summary open intentions except new patient")
  fun `when an existing patient is missing a phone number, a BP hasn't been recorded yet, and the user has never been reminded, then add phone dialog should not be shown`(
      openIntention: OpenIntention
  ) {
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(None))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.just(false))
    whenever(missingPhoneReminderRepository.markReminderAsShownFor(patientUuid)).doReturn(Completable.complete())

    setupController(openIntention)

    verify(ui, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  @Parameters(method = "patient summary open intentions except new patient")
  fun `when an existing patient is missing a phone number, and the user has been reminded before, then add phone dialog should not be shown`(
      openIntention: OpenIntention
  ) {
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(None))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.just(true))

    setupController(openIntention)

    verify(ui, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  @Parameters(method = "patient summary open intentions except new patient")
  fun `when an existing patient has a phone number, then add phone dialog should not be shown`(openIntention: OpenIntention) {
    val phoneNumber = Just(PatientMocker.phoneNumber(number = "101"))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(phoneNumber))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.never())

    setupController(openIntention)

    verify(ui, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  @Parameters(method = "patient summary open intentions except new patient")
  fun `when a new patient has a phone number, then add phone dialog should not be shown`(openIntention: OpenIntention) {
    val phoneNumber = Just(PatientMocker.phoneNumber(number = "101"))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(phoneNumber))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.never())

    setupController(openIntention)

    verify(ui, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  @Parameters(method = "patient summary open intentions except new patient")
  fun `when a new patient is missing a phone number, then add phone dialog should not be shown`(openIntention: OpenIntention) {
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(None))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.just(false))

    setupController(openIntention)

    verify(ui, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  private fun randomPatientSummaryOpenIntention() = `patient summary open intentions`().shuffled().first()

  @Suppress("Unused")
  private fun `patient summary open intentions`() = listOf(
      OpenIntention.ViewExistingPatient,
      OpenIntention.ViewNewPatient,
      OpenIntention.LinkIdWithPatient(Identifier("06293b71-0f56-45dc-845e-c05ee4d74153", BpPassport))
  )

  @Suppress("Unused")
  private fun `patient summary open intentions and screen to go back`(): List<List<Any>> {
    fun testCase(openIntention: OpenIntention, goBackToScreen: GoBackToScreen): List<Any> {
      return listOf(openIntention, goBackToScreen)
    }

    return listOf(
        testCase(openIntention = OpenIntention.ViewExistingPatient, goBackToScreen = PREVIOUS),
        testCase(openIntention = OpenIntention.ViewNewPatient, goBackToScreen = HOME),
        testCase(openIntention = OpenIntention.LinkIdWithPatient(Identifier("06293b71-0f56-45dc-845e-c05ee4d74153", BpPassport)), goBackToScreen = HOME)
    )
  }

  @Suppress("Unused")
  private fun `patient summary open intentions except new patient`() = listOf(
      OpenIntention.ViewExistingPatient,
      OpenIntention.LinkIdWithPatient(Identifier("06293b71-0f56-45dc-845e-c05ee4d74153", BpPassport))
  )

  @Suppress("Unused")
  private fun `patient summary open intentions and summary item changed`(): List<List<Any>> {
    val identifier = Identifier("06293b71-0f56-45dc-845e-c05ee4d74153", BpPassport)

    return listOf(
        listOf(OpenIntention.ViewExistingPatient, true),
        listOf(OpenIntention.ViewExistingPatient, false),
        listOf(OpenIntention.ViewNewPatient, true),
        listOf(OpenIntention.ViewNewPatient, false),
        listOf(OpenIntention.LinkIdWithPatient(identifier), true),
        listOf(OpenIntention.LinkIdWithPatient(identifier), false))
  }


  @Test
  @Parameters(method = "params for testing link id with patient bottom sheet")
  fun `link id with patient bottom sheet should only open when patient summary is created with link id intent`(
      openIntention: OpenIntention,
      shouldShowLinkIdSheet: Boolean,
      identifier: Identifier?
  ) {
    setupController(openIntention)

    if (shouldShowLinkIdSheet) {
      verify(ui).showLinkIdWithPatientView(patientUuid, identifier!!)
    } else {
      verify(ui, never()).showLinkIdWithPatientView(any(), any())
    }
  }

  @Suppress("Unused")
  private fun `params for testing link id with patient bottom sheet`(): List<Any> {
    val identifier = Identifier("1f79f976-f1bc-4c8a-8a53-ad646ce09fdb", BpPassport)

    return listOf(
        listOf(OpenIntention.LinkIdWithPatient(identifier), true, identifier),
        listOf(OpenIntention.ViewExistingPatient, false, null),
        listOf(OpenIntention.ViewNewPatient, false, null)
    )
  }

  @Test
  fun `when the link id with patient is cancelled, the patient summary screen must be closed`() {
    val openIntention = OpenIntention.LinkIdWithPatient(identifier = Identifier("id", BpPassport))
    setupController(openIntention)

    uiEvents.onNext(PatientSummaryLinkIdCancelled)

    verify(ui).goToPreviousScreen()
  }

  @Test
  fun `when the link id with patient is completed, the link id screen must be closed`() {
    val openIntention = OpenIntention.LinkIdWithPatient(identifier = Identifier("id", BpPassport))
    setupController(openIntention)

    uiEvents.onNext(PatientSummaryLinkIdCompleted)

    verify(ui).hideLinkIdWithPatientView()
    verify(ui, never()).goToPreviousScreen()
  }

  enum class GoBackToScreen {
    HOME,
    PREVIOUS
  }

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `when there are patient summary changes and at least one BP is present, clicking on back must show the schedule appointment sheet`(
      openIntention: OpenIntention
  ) {
    whenever(patientRepository.hasPatientDataChangedSince(any(), any())).doReturn(true)
    whenever(bpRepository.bloodPressureCount(patientUuid)).doReturn(1)

    setupController(openIntention)
    startMobiusLoop()
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(ui, never()).goToPreviousScreen()
    verify(ui, never()).goToHomeScreen()
    verify(ui).showScheduleAppointmentSheet(patientUuid)
  }

  @Test
  @Parameters(method = "params for going back or home when clicking back when there are no BPs")
  fun `when there are patient summary changes and all bps are deleted, clicking on back must go back`(
      openIntention: OpenIntention,
      goBackToScreen: GoBackToScreen
  ) {
    whenever(bpRepository.bloodPressureCount(patientUuid)).doReturn(0)

    setupController(openIntention)
    startMobiusLoop()
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(ui, never()).showScheduleAppointmentSheet(patientUuid)
    if (goBackToScreen == HOME) {
      verify(ui).goToHomeScreen()
    } else {
      verify(ui).goToPreviousScreen()
    }
  }

  @Suppress("Unused")
  private fun `params for going back or home when clicking back when there are no BPs`(): List<List<Any>> {
    return listOf(
        listOf(
            OpenIntention.ViewExistingPatient,
            PREVIOUS
        ),
        listOf(
            OpenIntention.ViewNewPatient,
            HOME
        ),
        listOf(
            OpenIntention.LinkIdWithPatient(Identifier("1f79f976-f1bc-4c8a-8a53-ad646ce09fdb", BpPassport)),
            HOME
        )
    )
  }

  @Test
  @Parameters(method = "patient summary open intentions and screen to go back")
  fun `when there are no patient summary changes and all bps are not deleted, clicking on back must go back`(
      openIntention: OpenIntention,
      goBackToScreen: GoBackToScreen
  ) {
    whenever(bpRepository.bloodPressureCount(patientUuid)).doReturn(1)

    setupController(openIntention)
    startMobiusLoop()
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(ui, never()).showScheduleAppointmentSheet(patientUuid)
    if (goBackToScreen == HOME) {
      verify(ui).goToHomeScreen()
    } else {
      verify(ui).goToPreviousScreen()
    }
  }

  @Test
  @Parameters(method = "patient summary open intentions and screen to go back")
  fun `when there are no patient summary changes and all bps are deleted, clicking on back must go back`(
      openIntention: OpenIntention,
      goBackToScreen: GoBackToScreen
  ) {
    whenever(bpRepository.bloodPressureCount(patientUuid)).doReturn(0)

    setupController(openIntention)
    startMobiusLoop()
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(ui, never()).showScheduleAppointmentSheet(patientUuid)
    if (goBackToScreen == HOME) {
      verify(ui).goToHomeScreen()
    } else {
      verify(ui).goToPreviousScreen()
    }
  }

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `when all bps are not deleted, clicking on save must show the schedule appointment sheet regardless of summary changes`(
      openIntention: OpenIntention
  ) {
    whenever(bpRepository.bloodPressureCount(patientUuid)).doReturn(1)

    setupController(openIntention)
    startMobiusLoop()
    uiEvents.onNext(PatientSummaryDoneClicked())

    verify(ui).showScheduleAppointmentSheet(patientUuid)
    verify(ui, never()).goToHomeScreen()
    verify(ui, never()).goToPreviousScreen()
  }

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `when all bps are deleted, clicking on save must go to the home screen regardless of summary changes`(
      openIntention: OpenIntention
  ) {
    whenever(bpRepository.bloodPressureCount(patientUuid)).doReturn(0)

    setupController(openIntention)
    startMobiusLoop()
    uiEvents.onNext(PatientSummaryDoneClicked())

    verify(ui, never()).showScheduleAppointmentSheet(patientUuid)
    verify(ui, never()).goToPreviousScreen()
    verify(ui).goToHomeScreen()
  }

  private fun setupController(
      openIntention: OpenIntention,
      patientUuid: UUID = this.patientUuid,
      screenCreatedTimestamp: Instant = Instant.parse("2018-01-01T00:00:00Z")
  ) {
    val controller = PatientSummaryScreenController(
        patientUuid = patientUuid,
        openIntention = openIntention,
        screenCreatedTimestamp = screenCreatedTimestamp,
        patientRepository = patientRepository,
        bpRepository = bpRepository,
        appointmentRepository = appointmentRepository,
        missingPhoneReminderRepository = missingPhoneReminderRepository
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }

    uiEvents.onNext(ScreenCreated())
  }

  private fun startMobiusLoop() {
    testFixture.start()
  }
}
