package org.simple.clinic.summary

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
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
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Duration
import java.util.Optional
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSummaryScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<PatientSummaryScreenUi>()
  private val uiActions = mock<PatientSummaryUiActions>()
  private val patientRepository = mock<PatientRepository>()
  private val bpRepository = mock<BloodPressureRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientUuid = UUID.fromString("d2fe1916-b76a-4bb6-b7e5-e107f00c3163")
  private val user = TestData.loggedInUser(UUID.fromString("3002c0e2-01ce-4053-833c-bc6f3aa3e3d4"))
  private val facility = TestData.facility(uuid = UUID.fromString("b84a6311-6faf-4de3-9336-ccd64de629f9"))
  private val medicalHistoryUuid = UUID.fromString("fe66d59b-b7e9-48f3-b22b-14d55c5532cb")
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val patientProfile = TestData.patientProfile(
      patientUuid = patientUuid,
      generatePhoneNumber = true,
      generateBusinessId = true
  )

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val viewRenderer = PatientSummaryViewRenderer(ui, modelUpdateCallback = { /* no-op */ })

  private lateinit var testFixture: MobiusTestFixture<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect>

  @Before
  fun setUp() {
    whenever(patientRepository.patientProfile(patientUuid)) doReturn Observable.just<Optional<PatientProfile>>(Optional.of(patientProfile))
    whenever(patientRepository.latestPhoneNumberForPatient(patientUuid)) doReturn Optional.empty()
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)) doReturn Optional.empty()
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
    val effectHandler = PatientSummaryEffectHandler(
        schedulersProvider = TrampolineSchedulersProvider(),
        patientRepository = patientRepository,
        bloodPressureRepository = bpRepository,
        appointmentRepository = appointmentRepository,
        missingPhoneReminderRepository = mock(),
        bloodSugarRepository = bloodSugarRepository,
        dataSync = mock(),
        medicalHistoryRepository = medicalHistoryRepository,
        country = TestData.country(),
        currentUser = Lazy { user },
        currentFacility = Lazy { facility },
        uuidGenerator = FakeUuidGenerator.fixed(medicalHistoryUuid),
        facilityRepository = facilityRepository,
        teleconsultationFacilityRepository = mock(),
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
