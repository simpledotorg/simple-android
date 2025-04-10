package org.simple.clinic.summary

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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.cvdrisk.CVDRiskRepository
import org.simple.clinic.cvdrisk.calculator.LabBasedCVDRiskCalculator
import org.simple.clinic.cvdrisk.calculator.NonLabBasedCVDRiskCalculator
import org.simple.clinic.drugs.DiagnosisWarningPrescriptions
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patientattribute.PatientAttributeRepository
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewNewPatient
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Duration
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSummaryScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val userClock = TestUserClock()
  private val utcClock = TestUtcClock()
  private val ui = mock<PatientSummaryScreenUi>()
  private val uiActions = mock<PatientSummaryUiActions>()
  private val patientRepository = mock<PatientRepository>()
  private val bpRepository = mock<BloodPressureRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val cvdRiskRepository = mock<CVDRiskRepository>()
  private val patientAttributeRepository = mock<PatientAttributeRepository>()
  private val patientUuid = UUID.fromString("d2fe1916-b76a-4bb6-b7e5-e107f00c3163")
  private val assignedFacilityUuid = UUID.fromString("1cc402ff-e1a6-4f4c-8494-b7d6ed0228fa")
  private val user = TestData.loggedInUser(UUID.fromString("3002c0e2-01ce-4053-833c-bc6f3aa3e3d4"))
  private val facility = TestData.facility(uuid = UUID.fromString("b84a6311-6faf-4de3-9336-ccd64de629f9"))
  private val medicalHistoryUuid = UUID.fromString("fe66d59b-b7e9-48f3-b22b-14d55c5532cb")
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val patientProfile = TestData.patientProfile(
      patientUuid = patientUuid,
      generatePhoneNumber = true,
      generateBusinessId = true
  )
  private val uuidGenerator = FakeUuidGenerator.fixed(medicalHistoryUuid)

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val viewRenderer = PatientSummaryViewRenderer(
      ui = ui,
      modelUpdateCallback = { /* no-op */ },
      userClock = TestUserClock(LocalDate.parse("2018-01-01")),
      cdssOverdueLimit = 2
  )
  private val diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions(
      htnPrescriptions = listOf("amlodipine"),
      diabetesPrescriptions = listOf("metformin")
  )

  private lateinit var testFixture: MobiusTestFixture<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect>

  @Before
  fun setUp() {
    val today = LocalDate.now(userClock)
        .atStartOfDay()
        .atZone(userClock.zone)
        .toInstant()

    whenever(bpRepository.isNewestBpEntryHigh(patientUuid)) doReturn Observable.just(true)
    whenever(patientRepository.patientProfile(patientUuid)) doReturn Observable.just(Optional.of(patientProfile))
    whenever(patientRepository.latestPhoneNumberForPatient(patientUuid)) doReturn Optional.empty()
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn Optional.empty()
    whenever(bpRepository.hasBPRecordedToday(patientUuid, today)) doReturn Observable.just(true)
    whenever(facilityRepository.facility(assignedFacilityUuid)) doReturn Optional.of(TestData.facility())
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        defaultHistoryUuid = uuidGenerator.v4(),
        patientUuid = patientUuid
    )) doReturn TestData.medicalHistory(uuid = medicalHistoryUuid)
    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn emptyList()
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
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn Optional.of(canceledAppointment)

    val phoneNumber = TestData.patientPhoneNumber(
        patientUuid = patientUuid,
        updatedAt = canceledAppointment.updatedAt - Duration.ofHours(2))
    whenever(patientRepository.latestPhoneNumberForPatient(patientUuid)) doReturn Optional.of(phoneNumber)

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
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn Optional.of(canceledAppointment)

    val phoneNumber = TestData.patientPhoneNumber(
        patientUuid = patientUuid,
        updatedAt = canceledAppointment.updatedAt + Duration.ofHours(2))
    whenever(patientRepository.latestPhoneNumberForPatient(patientUuid)) doReturn Optional.of(phoneNumber)

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
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn Optional.empty()

    startMobiusLoop(openIntention)

    verify(uiActions, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Test
  fun `when a new patient is missing a phone number, then avoid showing update phone dialog`() {
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn Optional.empty()

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

  private fun startMobiusLoop(openIntention: OpenIntention) {
    val viewEffectHandler = PatientSummaryViewEffectHandler(uiActions)
    val effectHandler = PatientSummaryEffectHandler(
        clock = utcClock,
        userClock = userClock,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        patientRepository = patientRepository,
        bloodPressureRepository = bpRepository,
        appointmentRepository = appointmentRepository,
        missingPhoneReminderRepository = mock(),
        bloodSugarRepository = bloodSugarRepository,
        dataSync = mock(),
        medicalHistoryRepository = medicalHistoryRepository,
        cvdRiskRepository = cvdRiskRepository,
        patientAttributeRepository = patientAttributeRepository,
        country = TestData.country(),
        currentUser = { user },
        currentFacility = { facility },
        uuidGenerator = uuidGenerator,
        facilityRepository = facilityRepository,
        teleconsultationFacilityRepository = mock(),
        prescriptionRepository = prescriptionRepository,
        cdssPilotFacilities = { emptyList() },
        diagnosisWarningPrescriptions = { diagnosisWarningPrescriptions },
        nonLabBasedCVDRiskCalculator = NonLabBasedCVDRiskCalculator { TestData.nonLabBasedCVDRiskCalculationSheet() },
        labBasedCVDRiskCalculator = LabBasedCVDRiskCalculator() { TestData.labBasedCVDRiskCalculationSheet() },
        viewEffectsConsumer = viewEffectHandler::handle
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = PatientSummaryModel.from(openIntention, patientUuid),
        init = PatientSummaryInit(),
        update = PatientSummaryUpdate(
            isPatientReassignmentFeatureEnabled = false,
            isPatientStatinNudgeV1Enabled = false,
            isNonLabBasedStatinNudgeEnabled = false,
            isLabBasedStatinNudgeEnabled = false,
        ),
        effectHandler = effectHandler.build(),
        modelUpdateListener = viewRenderer::render
    )

    testFixture.start()
  }
}
