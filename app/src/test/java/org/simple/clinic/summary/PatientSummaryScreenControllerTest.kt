package org.simple.clinic.summary

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewNewPatient
import org.simple.clinic.summary.teleconsultation.api.TeleconsultationApi
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import org.threeten.bp.Duration
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSummaryScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<PatientSummaryScreenUi>()
  private val uiActions = mock<PatientSummaryUiActions>()
  private val patientRepository = mock<PatientRepository>()
  private val bpRepository = mock<BloodPressureRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientUuid = UUID.fromString("d2fe1916-b76a-4bb6-b7e5-e107f00c3163")
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val teleconsultationApi = mock<TeleconsultationApi>()
  private val user = TestData.loggedInUser(UUID.fromString("3002c0e2-01ce-4053-833c-bc6f3aa3e3d4"))
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val patientProfile = TestData.patientProfile(
      patientUuid = patientUuid,
      generatePhoneNumber = true,
      generateBusinessId = true
  )
  private val patientSummaryConfig = PatientSummaryConfig(
      bpEditableDuration = Duration.ofMinutes(10),
      numberOfMeasurementsForTeleconsultation = 3
  )

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val viewRenderer = PatientSummaryViewRenderer(ui)

  private lateinit var testFixture: MobiusTestFixture<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect>

  @Before
  fun setUp() {
    whenever(patientRepository.patientProfile(patientUuid)) doReturn Observable.just<Optional<PatientProfile>>(Just(patientProfile))
    whenever(patientRepository.latestPhoneNumberForPatient(patientUuid)) doReturn None
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn None
    whenever(userSession.loggedInUserImmediate()).doReturn(user)
    whenever(facilityRepository.currentFacility(user)).doReturn(Observable.never())
  }

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  @Parameters(method = "appointment cancelation reasons")
  fun `when patient's phone was marked as invalid after the phone number was last updated then update phone dialog should be shown`(
      openIntention: OpenIntention,
      cancelReason: AppointmentCancelReason
  ) {
    val canceledAppointment = TestData.appointment(status = Cancelled, cancelReason = cancelReason)
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn Just(canceledAppointment)

    val phoneNumber = TestData.patientPhoneNumber(
        patientUuid = patientUuid,
        updatedAt = canceledAppointment.updatedAt - Duration.ofHours(2))
    whenever(patientRepository.latestPhoneNumberForPatient(patientUuid)) doReturn Just(phoneNumber)

    startMobiusLoop(openIntention)

    if (cancelReason == InvalidPhoneNumber) {
      verify(uiActions).showUpdatePhoneDialog(patientUuid)
    } else {
      verify(uiActions, never()).showUpdatePhoneDialog(patientUuid)
    }
  }

  @Test
  @Parameters(method = "appointment cancelation reasons")
  fun `when patient's phone was marked as invalid before the phone number was last updated then update phone dialog should not be shown`(
      openIntention: OpenIntention,
      cancelReason: AppointmentCancelReason
  ) {
    val canceledAppointment = TestData.appointment(status = Cancelled, cancelReason = cancelReason)
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn Just(canceledAppointment)

    val phoneNumber = TestData.patientPhoneNumber(
        patientUuid = patientUuid,
        updatedAt = canceledAppointment.updatedAt + Duration.ofHours(2))
    whenever(patientRepository.latestPhoneNumberForPatient(patientUuid)) doReturn Just(phoneNumber)

    startMobiusLoop(openIntention)

    verify(uiActions, never()).showUpdatePhoneDialog(patientUuid)
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
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn None

    startMobiusLoop(openIntention)

    verify(uiActions, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Test
  fun `when a new patient is missing a phone number, then avoid showing update phone dialog`() {
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn None

    startMobiusLoop(ViewExistingPatient)

    verify(uiActions, never()).showUpdatePhoneDialog(patientUuid)
  }

  private fun randomPatientSummaryOpenIntention() = `patient summary open intentions`().shuffled().first()

  @Suppress("Unused")
  private fun `patient summary open intentions`() = listOf(
      ViewExistingPatient,
      ViewNewPatient,
      LinkIdWithPatient(Identifier("06293b71-0f56-45dc-845e-c05ee4d74153", BpPassport))
  )

  @Suppress("Unused")
  private fun `patient summary open intentions and summary item changed`(): List<List<Any>> {
    val identifier = Identifier("06293b71-0f56-45dc-845e-c05ee4d74153", BpPassport)

    return listOf(
        listOf(ViewExistingPatient, true),
        listOf(ViewExistingPatient, false),
        listOf(ViewNewPatient, true),
        listOf(ViewNewPatient, false),
        listOf(LinkIdWithPatient(identifier), true),
        listOf(LinkIdWithPatient(identifier), false))
  }


  @Test
  @Parameters(method = "params for testing link id with patient bottom sheet")
  fun `link id with patient bottom sheet should only open when patient summary is created with link id intent`(
      openIntention: OpenIntention,
      shouldShowLinkIdSheet: Boolean,
      identifier: Identifier?
  ) {
    startMobiusLoop(openIntention)

    if (shouldShowLinkIdSheet) {
      verify(uiActions).showLinkIdWithPatientView(patientUuid, identifier!!)
    } else {
      verify(uiActions, never()).showLinkIdWithPatientView(any(), any())
    }
  }

  @Suppress("Unused")
  private fun `params for testing link id with patient bottom sheet`(): List<Any> {
    val identifier = Identifier("1f79f976-f1bc-4c8a-8a53-ad646ce09fdb", BpPassport)

    return listOf(
        listOf(LinkIdWithPatient(identifier), true, identifier),
        listOf(ViewExistingPatient, false, null),
        listOf(ViewNewPatient, false, null)
    )
  }

  @Test
  fun `when the link id with patient is cancelled, the patient summary screen must be closed`() {
    startMobiusLoop(LinkIdWithPatient(Identifier("abcd", BpPassport)))

    uiEvents.onNext(PatientSummaryLinkIdCancelled)

    verify(uiActions).goToPreviousScreen()
  }

  @Test
  fun `when the link id with patient is completed, the link id screen must be closed`() {
    val openIntention = LinkIdWithPatient(identifier = Identifier("id", BpPassport))
    startMobiusLoop(openIntention)

    uiEvents.onNext(PatientSummaryLinkIdCompleted)

    verify(uiActions).hideLinkIdWithPatientView()
    verify(uiActions, never()).goToPreviousScreen()
  }

  private fun startMobiusLoop(openIntention: OpenIntention) {
    val effectHandler = PatientSummaryEffectHandler(
        schedulersProvider = TrampolineSchedulersProvider(),
        patientRepository = patientRepository,
        bloodPressureRepository = bpRepository,
        appointmentRepository = appointmentRepository,
        missingPhoneReminderRepository = mock(),
        userSession = userSession,
        facilityRepository = facilityRepository,
        bloodSugarRepository = bloodSugarRepository,
        dataSync = mock(),
        medicalHistoryRepository = medicalHistoryRepository,
        prescriptionRepository = prescriptionRepository,
        country = TestData.country(),
        patientSummaryConfig = patientSummaryConfig,
        teleconsultationApi = teleconsultationApi,
        uiActions = uiActions
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = PatientSummaryModel.from(openIntention, patientUuid),
        init = PatientSummaryInit(),
        update = PatientSummaryUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = viewRenderer::render
    )

    testFixture.start()
  }
}
