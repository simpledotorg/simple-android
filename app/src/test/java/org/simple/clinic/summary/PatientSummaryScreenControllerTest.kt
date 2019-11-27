package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
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
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientMocker.medicalHistory
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
import org.simple.clinic.util.RelativeTimestamp.Today
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.randomMedicalHistoryAnswer
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID
import org.simple.clinic.summary.PatientSummaryScreenController as PatientSummaryScreenController1

@RunWith(JUnitParamsRunner::class)
class PatientSummaryScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<PatientSummaryScreenUi>()
  private val patientRepository = mock<PatientRepository>()
  private val bpRepository = mock<BloodPressureRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientUuid = UUID.fromString("d2fe1916-b76a-4bb6-b7e5-e107f00c3163")
  private val utcClock = TestUtcClock()
  private val userClock = TestUserClock()
  private val missingPhoneReminderRepository = mock<MissingPhoneReminderRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val reporter = MockAnalyticsReporter()
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
  private val dateFormatter = DateTimeFormatter.ISO_INSTANT
  private val zoneId = UTC
  private val bpDisplayLimit = 100

  private lateinit var controllerSubscription: Disposable

  @Before
  fun setUp() {
    whenever(patientRepository.patient(patientUuid)).doReturn(Observable.never())
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.never())
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, bpDisplayLimit)).doReturn(Observable.never())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).doReturn(Observable.never())
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).doReturn(Observable.never())
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).doReturn(Observable.never())
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.never())
    whenever(patientRepository.bpPassportForPatient(patientUuid)).doReturn(Observable.never())

    Analytics.addReporter(reporter)
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
    reporter.clear()
    controllerSubscription.dispose()
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

    setupControllerWithScreenCreated(intention)

    verify(ui).populatePatientProfile(PatientSummaryProfile(patient, address, phoneNumber, optionalBpPassport))
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
  fun `patient's prescription summary should be populated`(intention: OpenIntention) {
    val prescriptions = listOf(
        PatientMocker.prescription(name = "Amlodipine", dosage = "10mg"),
        PatientMocker.prescription(name = "Telmisartan", dosage = "9000mg"),
        PatientMocker.prescription(name = "Randomzole", dosage = "2 packets"))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).doReturn(Observable.just(prescriptions))
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, bpDisplayLimit)).doReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).doReturn(Observable.just(medicalHistory()))

    setupControllerWithScreenCreated(intention)

    verify(ui).populateList(eq(prescriptions), any(), any(), any())
  }

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `patient's blood pressure history should be populated`(intention: OpenIntention) {
    val bloodPressureMeasurements = listOf(
        PatientMocker.bp(patientUuid, systolic = 120, diastolic = 85, recordedAt = Instant.now(utcClock).minusSeconds(15L)),
        PatientMocker.bp(patientUuid, systolic = 164, diastolic = 95, recordedAt = Instant.now(utcClock).minusSeconds(30L)),
        PatientMocker.bp(patientUuid, systolic = 144, diastolic = 90, recordedAt = Instant.now(utcClock).minusSeconds(45L)))

    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, bpDisplayLimit)).doReturn(Observable.just(bloodPressureMeasurements))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).doReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).doReturn(Observable.just(medicalHistory()))

    setupControllerWithScreenCreated(intention)

    verify(ui).populateList(
        any<List<PrescribedDrug>>(),
        any(),
        check {
          it.forEachIndexed { i, item -> assertThat(item.measurement).isEqualTo(bloodPressureMeasurements[i]) }
        },
        any())
  }

  @Test
  @Parameters(method = "params for placeholder bp items")
  fun `the placeholder blood pressure items must be shown`(
      intention: OpenIntention,
      bloodPressureMeasurements: List<BloodPressureMeasurement>,
      expectedPlaceholderItems: List<SummaryBloodPressurePlaceholderListItem>,
      expectedBloodPressureMeasurementItems: List<SummaryBloodPressureListItem>
  ) {
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, bpDisplayLimit)).doReturn(Observable.just(bloodPressureMeasurements))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).doReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).doReturn(Observable.just(medicalHistory()))

    setupControllerWithScreenCreated(intention, numberOfBpPlaceholders = 3)

    verify(ui).populateList(
        prescribedDrugs = any(),
        measurementPlaceholderItems = eq(expectedPlaceholderItems),
        measurementItems = check {
          it.forEachIndexed { index, item -> assertThat(item.measurement).isEqualTo(expectedBloodPressureMeasurementItems[index].measurement) }
        },
        medicalHistory = any()
    )
  }

  @Suppress("Unused")
  private fun `params for placeholder bp items`(): List<List<Any>> {
    val bpsForTest1 = emptyList<BloodPressureMeasurement>()
    val bpsForTest2 = listOf(PatientMocker.bp(patientUuid))
    val bpsForTest3 = listOf(
        PatientMocker.bp(patientUuid),
        PatientMocker.bp(patientUuid)
    )
    val bpsForTest4 = listOf(
        PatientMocker.bp(patientUuid),
        PatientMocker.bp(patientUuid),
        PatientMocker.bp(patientUuid)
    )
    val bpsForTest5 = listOf(
        PatientMocker.bp(patientUuid, createdAt = Instant.now(utcClock).minus(1, ChronoUnit.DAYS)),
        PatientMocker.bp(patientUuid),
        PatientMocker.bp(patientUuid)
    )
    val bpsForTest6 = listOf(
        PatientMocker.bp(patientUuid, createdAt = Instant.now(utcClock).minus(2, ChronoUnit.DAYS)),
        PatientMocker.bp(patientUuid, createdAt = Instant.now(utcClock).minus(1, ChronoUnit.DAYS)),
        PatientMocker.bp(patientUuid)
    )

    val displayTime = "12:00 PM"

    // We won't be verifying the relative timestamps and showDivider in the test this is used in,
    // so we can just set it to a static value.
    return listOf(
        listOf(
            randomPatientSummaryOpenIntention(),
            bpsForTest1,
            listOf(
                SummaryBloodPressurePlaceholderListItem(1, true),
                SummaryBloodPressurePlaceholderListItem(2),
                SummaryBloodPressurePlaceholderListItem(3)
            ),
            emptyList<SummaryBloodPressureListItem>()
        ),
        listOf(
            randomPatientSummaryOpenIntention(),
            bpsForTest2,
            listOf(
                SummaryBloodPressurePlaceholderListItem(1),
                SummaryBloodPressurePlaceholderListItem(2)
            ),
            listOf(
                SummaryBloodPressureListItem(
                    measurement = bpsForTest2[0],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                )
            )
        ),
        listOf(
            randomPatientSummaryOpenIntention(),
            bpsForTest3,
            listOf(
                SummaryBloodPressurePlaceholderListItem(1),
                SummaryBloodPressurePlaceholderListItem(2)
            ),
            listOf(
                SummaryBloodPressureListItem(
                    measurement = bpsForTest3[0],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest3[1],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                )
            )
        ),
        listOf(
            randomPatientSummaryOpenIntention(),
            bpsForTest4,
            listOf(
                SummaryBloodPressurePlaceholderListItem(1),
                SummaryBloodPressurePlaceholderListItem(2)
            ),
            listOf(
                SummaryBloodPressureListItem(
                    measurement = bpsForTest4[0],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest4[1],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest4[2],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                )
            )
        ),
        listOf(
            randomPatientSummaryOpenIntention(),
            bpsForTest5,
            listOf(SummaryBloodPressurePlaceholderListItem(1)),
            listOf(
                SummaryBloodPressureListItem(
                    measurement = bpsForTest5[0],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest5[1],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest5[2],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                )
            )
        ),
        listOf(
            randomPatientSummaryOpenIntention(),
            bpsForTest6,
            emptyList<SummaryBloodPressurePlaceholderListItem>(),
            listOf(
                SummaryBloodPressureListItem(
                    measurement = bpsForTest6[0],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest6[1],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest6[2],
                    showDivider = true,
                    formattedTime = displayTime,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                )
            )
        )
    )
  }

  @Test
  fun `when a bp is created then it should be editable till a fixed config duration`() {
    //given
    val now = Instant.now(utcClock)

    val bps = listOf(
        PatientMocker.bp(
            uuid = UUID.fromString("2077cea4-c880-4163-a577-74cab23741a6"),
            patientUuid = patientUuid,
            createdAt = now,
            recordedAt = now
        ),
        PatientMocker.bp(
            uuid = UUID.fromString("5ed902c6-8c77-4a88-ba57-0cfb31a419fe"),
            patientUuid = patientUuid,
            createdAt = now.minus(10, ChronoUnit.MINUTES),
            recordedAt = now.minus(1, ChronoUnit.DAYS)
        )
    )

    val numberOfBpsToDisplay = 3
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, numberOfBpsToDisplay)).doReturn(Observable.just(bps))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).doReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).doReturn(Observable.just(medicalHistory()))

    //when
    setupControllerWithScreenCreated(
        openIntention = OpenIntention.ViewExistingPatient,
        numberOfBpsToDisplay = numberOfBpsToDisplay,
        bpEditableDuration = Duration.ofSeconds(3600),
        screenCreatedTimestamp = now
    )

    verify(ui).populateList(
        prescribedDrugs = any(),
        measurementPlaceholderItems = any(),
        measurementItems = check {
          it.forEachIndexed { index, item -> assertThat(item.isBpEditable).isTrue() }
        },
        medicalHistory = any()
    )
  }

  @Test
  fun `when a bp is created more than the fixed config duration ago then it should not be editable`() {
    //given
    val permittedDuration = Duration.ofMinutes(60)
    val now = Instant.now(utcClock)

    val bps = listOf(
        PatientMocker.bp(
            uuid = UUID.fromString("2077cea4-c880-4163-a577-74cab23741a6"),
            patientUuid = patientUuid,
            createdAt = now.minus(1, ChronoUnit.DAYS),
            recordedAt = now
        ),
        PatientMocker.bp(
            uuid = UUID.fromString("5ed902c6-8c77-4a88-ba57-0cfb31a419fe"),
            patientUuid = patientUuid,
            createdAt = now.minus(70, ChronoUnit.MINUTES),
            recordedAt = now.minus(1, ChronoUnit.DAYS)
        )
    )

    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, bpDisplayLimit)).doReturn(Observable.just(bps))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).doReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).doReturn(Observable.just(medicalHistory()))

    //when
    setupControllerWithScreenCreated(
        openIntention = OpenIntention.ViewExistingPatient,
        bpEditableDuration = permittedDuration,
        screenCreatedTimestamp = now
    )

    verify(ui).populateList(
        prescribedDrugs = any(),
        measurementPlaceholderItems = any(),
        measurementItems = check {
          it.forEachIndexed { index, item -> assertThat(item.isBpEditable).isFalse() }
        },
        medicalHistory = any()
    )
  }

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `patient's medical history should be populated`(openIntention: OpenIntention) {
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).doReturn(Observable.just(emptyList()))
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, bpDisplayLimit)).doReturn(Observable.just(emptyList()))

    val medicalHistory = medicalHistory(updatedAt = Instant.now(utcClock))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).doReturn(Observable.just(medicalHistory))

    setupControllerWithScreenCreated(openIntention)

    verify(ui).populateList(any(), any(), any(), eq(medicalHistory))
  }

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `when new-BP is clicked then BP entry sheet should be shown`(openIntention: OpenIntention) {
    setupControllerWithScreenCreated(openIntention)
    uiEvents.onNext(PatientSummaryNewBpClicked())

    verify(ui, times(1)).showBloodPressureEntrySheet(patientUuid)
  }

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `when update medicines is clicked then BP medicines screen should be shown`(openIntention: OpenIntention) {
    setupControllerWithScreenCreated(openIntention)
    uiEvents.onNext(PatientSummaryUpdateDrugsClicked())

    verify(ui).showUpdatePrescribedDrugsScreen(patientUuid)
  }

  @Test
  @Parameters(method = "patient summary open intentions")
  fun `when the screen is opened, the viewed patient analytics event must be sent`(openIntention: OpenIntention) {
    setupControllerWithScreenCreated(openIntention)

    val expectedEvent = MockAnalyticsReporter.Event("ViewedPatient", mapOf(
        "patientId" to patientUuid.toString(),
        "from" to openIntention.analyticsName()
    ))
    assertThat(reporter.receivedEvents).contains(expectedEvent)
  }

  @Test
  @Parameters(method = "medicalHistoryQuestionsAndAnswers")
  fun `when answers for medical history questions are toggled, then the updated medical history should be saved`(
      openIntention: OpenIntention,
      question: MedicalHistoryQuestion,
      newAnswer: Answer
  ) {
    val medicalHistory = medicalHistory(
        diagnosedWithHypertension = Unanswered,
        isOnTreatmentForHypertension = Unanswered,
        hasHadHeartAttack = Unanswered,
        hasHadStroke = Unanswered,
        hasHadKidneyDisease = Unanswered,
        hasDiabetes = Unanswered,
        updatedAt = Instant.now())
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).doReturn(Observable.just(medicalHistory))
    whenever(medicalHistoryRepository.save(any<MedicalHistory>(), any())).doReturn(Completable.complete())

    setupControllerWithScreenCreated(openIntention)
    uiEvents.onNext(SummaryMedicalHistoryAnswerToggled(question, answer = newAnswer))

    val updatedMedicalHistory = medicalHistory.copy(
        diagnosedWithHypertension = if (question == DIAGNOSED_WITH_HYPERTENSION) newAnswer else Unanswered,
        isOnTreatmentForHypertension = if (question == IS_ON_TREATMENT_FOR_HYPERTENSION) newAnswer else Unanswered,
        hasHadHeartAttack = if (question == HAS_HAD_A_HEART_ATTACK) newAnswer else Unanswered,
        hasHadStroke = if (question == HAS_HAD_A_STROKE) newAnswer else Unanswered,
        hasHadKidneyDisease = if (question == HAS_HAD_A_KIDNEY_DISEASE) newAnswer else Unanswered,
        hasDiabetes = if (question == HAS_DIABETES) newAnswer else Unanswered)
    verify(medicalHistoryRepository).save(eq(updatedMedicalHistory), any())
  }

  @Suppress("unused")
  fun medicalHistoryQuestionsAndAnswers(): List<List<Any>> {
    val questions = MedicalHistoryQuestion.values().asList()
    return questions
        .asSequence()
        .map { question ->
          listOf(
              randomPatientSummaryOpenIntention(),
              question,
              randomMedicalHistoryAnswer()
          )
        }
        .toList()
  }

  @Test
  fun `when blood pressure is clicked for editing, blood pressure update sheet should show up`() {
    setupControllerWithoutScreenCreated()

    val bloodPressureMeasurement = PatientMocker.bp()
    uiEvents.onNext(PatientSummaryBpClicked(bloodPressureMeasurement))

    verify(ui).showBloodPressureUpdateSheet(bloodPressureMeasurement.uuid)
  }

  @Test
  @Parameters(method = "params for BP grouped by date")
  fun `when BPs are grouped by dates, then only the last BP item in every group should show divider`(
      openIntention: OpenIntention,
      bloodPressureMeasurements: List<BloodPressureMeasurement>,
      expectedBloodPressureMeasurementItems: List<SummaryBloodPressureListItem>
  ) {
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, bpDisplayLimit)).doReturn(Observable.just(bloodPressureMeasurements))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).doReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).doReturn(Observable.just(medicalHistory()))

    setupControllerWithScreenCreated(openIntention)

    verify(ui).populateList(
        prescribedDrugs = any(),
        measurementPlaceholderItems = any(),
        measurementItems = check {
          it.forEachIndexed { index, item ->
            assertThat(item.measurement).isEqualTo(expectedBloodPressureMeasurementItems[index].measurement)
            assertThat(item.showDivider).isEqualTo(expectedBloodPressureMeasurementItems[index].showDivider)
            assertThat(item.formattedTime).isEqualTo(expectedBloodPressureMeasurementItems[index].formattedTime)
            assertThat(item.dateFormatter).isSameAs(dateFormatter)
          }
        },
        medicalHistory = any()
    )
  }

  @Suppress("unused")
  private fun `params for BP grouped by date`(): List<List<Any>> {
    val bpsForTest1 = listOf(
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.now(utcClock).plusMillis(1000)),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.now(utcClock).plusMillis(500)),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.now(utcClock).minus(1, ChronoUnit.DAYS))
    )
    val bpsForTest2 = listOf(
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.now(utcClock)),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.now(utcClock)),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.now(utcClock).minus(1, ChronoUnit.DAYS)),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.now(utcClock).minus(1, ChronoUnit.DAYS).plusMillis(1000)),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.now(utcClock).minus(2, ChronoUnit.DAYS))
    )

    val displayTime = { instant: Instant ->
      instant.atZone(zoneId).format(timeFormatter)
    }

    return listOf(
        listOf(
            randomPatientSummaryOpenIntention(),
            bpsForTest1,
            listOf(
                SummaryBloodPressureListItem(
                    measurement = bpsForTest1[0],
                    showDivider = false,
                    formattedTime = displayTime(bpsForTest1[0].recordedAt),
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest1[1],
                    showDivider = true,
                    formattedTime = displayTime(bpsForTest1[1].recordedAt),
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest1[2],
                    showDivider = true,
                    formattedTime = null,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                )
            )),
        listOf(
            randomPatientSummaryOpenIntention(),
            bpsForTest2,
            listOf(
                SummaryBloodPressureListItem(
                    measurement = bpsForTest2[0],
                    showDivider = false,
                    formattedTime = displayTime(bpsForTest2[0].recordedAt),
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest2[1],
                    showDivider = true,
                    formattedTime = displayTime(bpsForTest2[1].recordedAt),
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest2[2],
                    showDivider = false,
                    formattedTime = displayTime(bpsForTest2[2].recordedAt),
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest2[3],
                    showDivider = true,
                    formattedTime = displayTime(bpsForTest2[3].recordedAt),
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                ),
                SummaryBloodPressureListItem(
                    measurement = bpsForTest2[4],
                    showDivider = true,
                    formattedTime = null,
                    addTopPadding = false,
                    daysAgo = Today,
                    dateFormatter = dateFormatter,
                    isBpEditable = false
                )
            ))
    )
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

    setupControllerWithScreenCreated(openIntention)

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

    setupControllerWithScreenCreated(openIntention)

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

    setupControllerWithScreenCreated(openIntention)

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

    setupControllerWithScreenCreated(openIntention)

    verify(ui, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Test
  fun `when a new patient is missing a phone number, then avoid showing update phone dialog`() {
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).doReturn(Observable.just<Optional<Appointment>>(None))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(None))

    setupControllerWithScreenCreated(OpenIntention.ViewNewPatient)

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

    setupControllerWithScreenCreated(openIntention)
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

    setupControllerWithScreenCreated(openIntention)

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

    setupControllerWithScreenCreated(openIntention)

    verify(ui, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  @Parameters(method = "patient summary open intentions except new patient")
  fun `when an existing patient has a phone number, then add phone dialog should not be shown`(openIntention: OpenIntention) {
    val phoneNumber = Just(PatientMocker.phoneNumber(number = "101"))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(phoneNumber))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.never())

    setupControllerWithScreenCreated(openIntention)

    verify(ui, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  @Parameters(method = "patient summary open intentions except new patient")
  fun `when a new patient has a phone number, then add phone dialog should not be shown`(openIntention: OpenIntention) {
    val phoneNumber = Just(PatientMocker.phoneNumber(number = "101"))
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(phoneNumber))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.never())

    setupControllerWithScreenCreated(openIntention)

    verify(ui, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  @Parameters(method = "patient summary open intentions except new patient")
  fun `when a new patient is missing a phone number, then add phone dialog should not be shown`(openIntention: OpenIntention) {
    whenever(patientRepository.phoneNumber(patientUuid)).doReturn(Observable.just<Optional<PatientPhoneNumber>>(None))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).doReturn(Single.just(false))

    setupControllerWithScreenCreated(openIntention)

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
    setupControllerWithScreenCreated(openIntention)

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
    setupControllerWithScreenCreated(openIntention)

    uiEvents.onNext(PatientSummaryLinkIdCancelled)

    verify(ui).goToPreviousScreen()
  }

  @Test
  fun `when the link id with patient is completed, the link id screen must be closed`() {
    val openIntention = OpenIntention.LinkIdWithPatient(identifier = Identifier("id", BpPassport))
    setupControllerWithScreenCreated(openIntention)

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

    setupControllerWithScreenCreated(openIntention)
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

    setupControllerWithScreenCreated(openIntention)
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

    setupControllerWithScreenCreated(openIntention)
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

    setupControllerWithScreenCreated(openIntention)
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

    setupControllerWithScreenCreated(openIntention)
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

    setupControllerWithScreenCreated(openIntention)
    uiEvents.onNext(PatientSummaryDoneClicked())

    verify(ui, never()).showScheduleAppointmentSheet(patientUuid)
    verify(ui, never()).goToPreviousScreen()
    verify(ui).goToHomeScreen()
  }

  private fun setupControllerWithScreenCreated(
      openIntention: OpenIntention,
      patientUuid: UUID = this.patientUuid,
      screenCreatedTimestamp: Instant = Instant.now(utcClock),
      numberOfBpPlaceholders: Int = 0,
      numberOfBpsToDisplay: Int = this.bpDisplayLimit,
      bpEditableDuration: Duration = Duration.ofMinutes(60)
  ) {
    val config = PatientSummaryConfig(numberOfBpPlaceholders, numberOfBpsToDisplay, bpEditableDuration)
    createController(config)

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, openIntention, screenCreatedTimestamp))
  }

  private fun setupControllerWithoutScreenCreated(
      numberOfBpPlaceholders: Int = 0,
      numberOfBpsToDisplay: Int = this.bpDisplayLimit,
      bpEditableDuration: Duration = Duration.ofMinutes(60)
  ) {
    val config = PatientSummaryConfig(numberOfBpPlaceholders, numberOfBpsToDisplay, bpEditableDuration)
    createController(config)
  }

  private fun createController(config: PatientSummaryConfig) {
    val controller = PatientSummaryScreenController1(
        patientRepository = patientRepository,
        bpRepository = bpRepository,
        prescriptionRepository = prescriptionRepository,
        medicalHistoryRepository = medicalHistoryRepository,
        appointmentRepository = appointmentRepository,
        missingPhoneReminderRepository = missingPhoneReminderRepository,
        timestampGenerator = RelativeTimestampGenerator(),
        utcClock = utcClock,
        userClock = userClock,
        zoneId = zoneId,
        config = config,
        timeFormatterForBp = timeFormatter,
        exactDateFormatter = dateFormatter
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }
  }
}
