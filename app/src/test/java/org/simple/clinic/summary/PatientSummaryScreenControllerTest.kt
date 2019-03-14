package org.simple.clinic.summary

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
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
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.UNKNOWN
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.Appointment.Status.CANCELLED
import org.simple.clinic.overdue.Appointment.Status.SCHEDULED
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientMocker.medicalHistory
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSummaryResult
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSummaryScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<PatientSummaryScreen>()
  private val patientRepository = mock<PatientRepository>()
  private val bpRepository = mock<BloodPressureRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val patientUuid = UUID.randomUUID()
  private val clock = TestUtcClock()
  private val patientSummaryResult = mock<Preference<PatientSummaryResult>>()
  private val missingPhoneReminderRepository = mock<MissingPhoneReminderRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val configSubject = BehaviorSubject.create<PatientSummaryConfig>()
  private val reporter = MockAnalyticsReporter()
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
  private val zoneId = UTC

  private lateinit var controller: PatientSummaryScreenController

  @Before
  fun setUp() {
    val timestampGenerator = RelativeTimestampGenerator()
    controller = PatientSummaryScreenController(
        patientRepository = patientRepository,
        bpRepository = bpRepository,
        prescriptionRepository = prescriptionRepository,
        medicalHistoryRepository = medicalHistoryRepository,
        appointmentRepository = appointmentRepository,
        missingPhoneReminderRepository = missingPhoneReminderRepository,
        timestampGenerator = timestampGenerator,
        utcClock = clock,
        zoneId = zoneId,
        configProvider = configSubject.firstOrError(),
        patientSummaryResult = patientSummaryResult,
        timeFormatterForBp = timeFormatter)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    whenever(patientRepository.patient(patientUuid)).thenReturn(Observable.never())
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.never())
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, 100)).thenReturn(Observable.never())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.never())
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.never())
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).thenReturn(Observable.never())
    whenever(bpRepository.bloodPressureCount(patientUuid)).thenReturn(Observable.just(1))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).thenReturn(Single.never())

    Analytics.addReporter(reporter)
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
    reporter.clear()
  }

  @Test
  fun `patient's profile should be populated`() {
    val addressUuid = UUID.randomUUID()
    val patient = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid)
    val address = PatientMocker.address(uuid = addressUuid)
    val phoneNumber = None

    whenever(patientRepository.patient(patientUuid)).thenReturn(Observable.just(Just(patient)))
    whenever(patientRepository.address(addressUuid)).thenReturn(Observable.just(Just(address)))
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(phoneNumber))
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, 100)).thenReturn(Observable.never())

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT, screenCreatedTimestamp = Instant.now(clock)))

    verify(screen).populatePatientProfile(patient, address, phoneNumber)
  }

  @Test
  fun `patient's prescription summary should be populated`() {
    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    val prescriptions = listOf(
        PatientMocker.prescription(name = "Amlodipine", dosage = "10mg"),
        PatientMocker.prescription(name = "Telmisartan", dosage = "9000mg"),
        PatientMocker.prescription(name = "Randomzole", dosage = "2 packets"))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(prescriptions))
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, config.numberOfBpsToDisplay)).thenReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(medicalHistory()))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now(clock)))

    verify(screen).populateList(eq(SummaryPrescribedDrugsItem(prescriptions)), any(), any(), any())
  }

  @Test
  fun `patient's blood pressure history should be populated`() {
    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    val bloodPressureMeasurements = listOf(
        PatientMocker.bp(patientUuid, systolic = 120, diastolic = 85, createdAt = Instant.now(clock).minusSeconds(15L)),
        PatientMocker.bp(patientUuid, systolic = 164, diastolic = 95, createdAt = Instant.now(clock).minusSeconds(30L)),
        PatientMocker.bp(patientUuid, systolic = 144, diastolic = 90, createdAt = Instant.now(clock).minusSeconds(45L)))

    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, config.numberOfBpsToDisplay)).thenReturn(Observable.just(bloodPressureMeasurements))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(medicalHistory()))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT, screenCreatedTimestamp = Instant.now(clock)))

    verify(screen).populateList(
        any(),
        any(),
        check {
          it.forEachIndexed { i, item -> assertThat(item.measurement).isEqualTo(bloodPressureMeasurements[i]) }
        },
        any())
  }

  @Test
  @Parameters(method = "params for placeholder bp items")
  fun `the placeholder blood pressure items must be shown`(
      bloodPressureMeasurements: List<BloodPressureMeasurement>,
      expectedPlaceholderItems: List<SummaryBloodPressurePlaceholderListItem>,
      expectedBloodPressureMeasurementItems: List<SummaryBloodPressureListItem>
  ) {
    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 3,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, config.numberOfBpsToDisplay)).thenReturn(Observable.just(bloodPressureMeasurements))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(medicalHistory()))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT, screenCreatedTimestamp = Instant.now(clock)))

    verify(screen).populateList(
        prescribedDrugsItem = any(),
        measurementPlaceholderItems = eq(expectedPlaceholderItems),
        measurementItems = check {
          it.forEachIndexed { index, item -> assertThat(item.measurement).isEqualTo(expectedBloodPressureMeasurementItems[index].measurement) }
        },
        medicalHistoryItem = any()
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
        PatientMocker.bp(patientUuid, createdAt = Instant.now(clock).minus(1, ChronoUnit.DAYS)),
        PatientMocker.bp(patientUuid),
        PatientMocker.bp(patientUuid)
    )
    val bpsForTest6 = listOf(
        PatientMocker.bp(patientUuid, createdAt = Instant.now(clock).minus(2, ChronoUnit.DAYS)),
        PatientMocker.bp(patientUuid, createdAt = Instant.now(clock).minus(1, ChronoUnit.DAYS)),
        PatientMocker.bp(patientUuid)
    )

    val displayTime = "12:00 PM"

    // We won't be verifying the relative timestamps and showDivider in the test this is used in,
    // so we can just set it to a static value.
    return listOf(
        listOf<Any>(
            bpsForTest1,
            listOf(
                SummaryBloodPressurePlaceholderListItem(1, true),
                SummaryBloodPressurePlaceholderListItem(2),
                SummaryBloodPressurePlaceholderListItem(3)
            ),
            emptyList<SummaryBloodPressureListItem>()
        ),
        listOf<Any>(
            bpsForTest2,
            listOf(
                SummaryBloodPressurePlaceholderListItem(1),
                SummaryBloodPressurePlaceholderListItem(2)
            ),
            listOf(
                SummaryBloodPressureListItem(measurement = bpsForTest2[0], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false)
            )
        ),
        listOf<Any>(
            bpsForTest3,
            listOf(
                SummaryBloodPressurePlaceholderListItem(1),
                SummaryBloodPressurePlaceholderListItem(2)
            ),
            listOf(
                SummaryBloodPressureListItem(measurement = bpsForTest3[0], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest3[1], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false)
            )
        ),
        listOf<Any>(
            bpsForTest4,
            listOf(
                SummaryBloodPressurePlaceholderListItem(1),
                SummaryBloodPressurePlaceholderListItem(2)
            ),
            listOf(
                SummaryBloodPressureListItem(measurement = bpsForTest4[0], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest4[1], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest4[2], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false)
            )
        ),
        listOf<Any>(
            bpsForTest5,
            listOf(SummaryBloodPressurePlaceholderListItem(1)),
            listOf(
                SummaryBloodPressureListItem(measurement = bpsForTest5[0], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest5[1], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest5[2], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false)
            )
        ),
        listOf<Any>(
            bpsForTest6,
            emptyList<SummaryBloodPressurePlaceholderListItem>(),
            listOf(
                SummaryBloodPressureListItem(measurement = bpsForTest6[0], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest6[1], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest6[2], daysAgo = Today, showDivider = true, formattedTime = displayTime, addTopPadding = false)
            )
        )
    )
  }

  @Test
  fun `patient's medical history should be populated`() {
    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(emptyList()))
    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, config.numberOfBpsToDisplay)).thenReturn(Observable.just(emptyList()))

    val medicalHistory = medicalHistory(updatedAt = Instant.now())
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(medicalHistory))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now(clock)))

    verify(screen).populateList(any(), any(), any(), eq(SummaryMedicalHistoryItem(medicalHistory, Today)))
  }

  @Test
  fun `when new-BP is clicked then BP entry sheet should be shown`() {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now(clock)))
    uiEvents.onNext(PatientSummaryNewBpClicked())

    verify(screen, times(1)).showBloodPressureEntrySheet(patientUuid)
    verify(screen, never()).showBloodPressureEntrySheetIfNotShownAlready(any())
  }

  @Test
  fun `when screen was opened after saving a new patient then BP entry sheet should be shown`() {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now(clock)))
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT, screenCreatedTimestamp = Instant.now(clock)))

    verify(screen, times(1)).showBloodPressureEntrySheetIfNotShownAlready(any())
    verify(screen, never()).showBloodPressureEntrySheet(any())
  }

  @Test
  @Parameters(method = "patient summary callers")
  fun `when there are patient summary changes and all bps are not deleted, clicking on back must show the schedule appointment sheet`(
      patientSummaryCaller: PatientSummaryCaller
  ) {
    val patientSummaryItems = mock<PatientSummaryItems>()
    whenever(patientSummaryItems.hasItemChangedSince(any())).thenReturn(true)

    uiEvents.onNext(PatientSummaryScreenCreated(
        patientUuid = patientUuid,
        caller = patientSummaryCaller,
        screenCreatedTimestamp = Instant.now(clock)
    ))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItems))
    uiEvents.onNext(PatientSummaryAllBloodPressuresDeleted(false))
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(screen, never()).goBackToPatientSearch()
    verify(screen, never()).goBackToHome()
    verify(screen).showScheduleAppointmentSheet(patientUuid)
  }

  @Test
  @Parameters(method = "patient summary callers")
  fun `when there are patient summary changes and all bps are deleted, clicking on back must go back`(
      patientSummaryCaller: PatientSummaryCaller
  ) {
    val patientSummaryItems = mock<PatientSummaryItems>()
    whenever(patientSummaryItems.hasItemChangedSince(any())).thenReturn(true)

    uiEvents.onNext(PatientSummaryScreenCreated(
        patientUuid = patientUuid,
        caller = patientSummaryCaller,
        screenCreatedTimestamp = Instant.now(clock)
    ))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItems))
    uiEvents.onNext(PatientSummaryAllBloodPressuresDeleted(true))
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(screen, never()).showScheduleAppointmentSheet(patientUuid)
    if (patientSummaryCaller == PatientSummaryCaller.NEW_PATIENT) {
      verify(screen).goBackToHome()
    } else {
      verify(screen).goBackToPatientSearch()
    }
  }

  @Test
  @Parameters(method = "patient summary callers")
  fun `when there are no patient summary changes and all bps are not deleted, clicking on back must go back`(
      patientSummaryCaller: PatientSummaryCaller
  ) {
    val patientSummaryItems = mock<PatientSummaryItems>()
    whenever(patientSummaryItems.hasItemChangedSince(any())).thenReturn(false)

    uiEvents.onNext(PatientSummaryScreenCreated(
        patientUuid = patientUuid,
        caller = patientSummaryCaller,
        screenCreatedTimestamp = Instant.now(clock)
    ))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItems))
    uiEvents.onNext(PatientSummaryAllBloodPressuresDeleted(false))
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(screen, never()).showScheduleAppointmentSheet(patientUuid)
    if (patientSummaryCaller == PatientSummaryCaller.NEW_PATIENT) {
      verify(screen).goBackToHome()
    } else {
      verify(screen).goBackToPatientSearch()
    }
  }

  @Test
  @Parameters(method = "patient summary callers")
  fun `when there are no patient summary changes and all bps are deleted, clicking on back must go back`(
      patientSummaryCaller: PatientSummaryCaller
  ) {
    val patientSummaryItems = mock<PatientSummaryItems>()
    whenever(patientSummaryItems.hasItemChangedSince(any())).thenReturn(false)

    uiEvents.onNext(PatientSummaryScreenCreated(
        patientUuid = patientUuid,
        caller = patientSummaryCaller,
        screenCreatedTimestamp = Instant.now(clock)
    ))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItems))
    uiEvents.onNext(PatientSummaryAllBloodPressuresDeleted(true))
    uiEvents.onNext(PatientSummaryBackClicked())

    verify(screen, never()).showScheduleAppointmentSheet(patientUuid)
    if (patientSummaryCaller == PatientSummaryCaller.NEW_PATIENT) {
      verify(screen).goBackToHome()
    } else {
      verify(screen).goBackToPatientSearch()
    }
  }

  @Suppress("Unused")
  private fun `patient summary callers`() = PatientSummaryCaller.values()

  @Test
  @Parameters(method = "patient summary callers and summary item changed")
  fun `when all bps are not deleted, clicking on save must show the schedule appointment sheet`(
      patientSummaryCaller: PatientSummaryCaller,
      patientSummaryItemChanged: Boolean
  ) {
    val patientSummaryItems = mock<PatientSummaryItems>()
    whenever(patientSummaryItems.hasItemChangedSince(any())).thenReturn(patientSummaryItemChanged)

    uiEvents.onNext(PatientSummaryScreenCreated(
        patientUuid = patientUuid,
        caller = patientSummaryCaller,
        screenCreatedTimestamp = Instant.now(clock)
    ))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItems))
    uiEvents.onNext(PatientSummaryAllBloodPressuresDeleted(false))
    uiEvents.onNext(PatientSummaryDoneClicked())

    verify(screen).showScheduleAppointmentSheet(patientUuid)
    verify(screen, never()).goBackToHome()
    verify(screen, never()).goBackToPatientSearch()
  }

  @Test
  @Parameters(method = "patient summary callers and summary item changed")
  fun `when all bps are deleted, clicking on save must go to the home screen`(
      patientSummaryCaller: PatientSummaryCaller,
      patientSummaryItemChanged: Boolean
  ) {
    val patientSummaryItems = mock<PatientSummaryItems>()
    whenever(patientSummaryItems.hasItemChangedSince(any())).thenReturn(patientSummaryItemChanged)

    uiEvents.onNext(PatientSummaryScreenCreated(
        patientUuid = patientUuid,
        caller = patientSummaryCaller,
        screenCreatedTimestamp = Instant.now(clock)
    ))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItems))
    uiEvents.onNext(PatientSummaryAllBloodPressuresDeleted(true))
    uiEvents.onNext(PatientSummaryDoneClicked())

    verify(screen, never()).showScheduleAppointmentSheet(patientUuid)
    verify(screen, never()).goBackToPatientSearch()
    verify(screen).goBackToHome()
  }

  @Suppress("Unused")
  private fun `patient summary callers and summary item changed`(): List<List<Any>> {
    return listOf(
        listOf(PatientSummaryCaller.SEARCH, true),
        listOf(PatientSummaryCaller.SEARCH, false),
        listOf(PatientSummaryCaller.NEW_PATIENT, true),
        listOf(PatientSummaryCaller.NEW_PATIENT, false))
  }

  @Test
  fun `when update medicines is clicked then BP medicines screen should be shown`() {
    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now(clock)))
    uiEvents.onNext(PatientSummaryUpdateDrugsClicked())

    verify(screen).showUpdatePrescribedDrugsScreen(patientUuid)
  }

  @Test
  @Parameters(
      "SEARCH",
      "NEW_PATIENT"
  )
  fun `when the screen is opened, the viewed patient analytics event must be sent`(fromCaller: PatientSummaryCaller) {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, fromCaller, Instant.now(clock)))

    val expectedEvent = MockAnalyticsReporter.Event("ViewedPatient", mapOf(
        "patientId" to patientUuid.toString(),
        "from" to fromCaller.name
    ))
    assertThat(reporter.receivedEvents).contains(expectedEvent)
  }

  @Test
  @Parameters(method = "medicalHistoryQuestionsAndAnswers")
  fun `when answers for medical history questions are toggled, then the updated medical history should be saved`(
      question: MedicalHistoryQuestion,
      newAnswer: MedicalHistory.Answer
  ) {
    val medicalHistory = medicalHistory(
        diagnosedWithHypertension = UNKNOWN,
        isOnTreatmentForHypertension = UNKNOWN,
        hasHadHeartAttack = UNKNOWN,
        hasHadStroke = UNKNOWN,
        hasHadKidneyDisease = UNKNOWN,
        hasDiabetes = UNKNOWN,
        updatedAt = Instant.now())
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(medicalHistory))
    whenever(medicalHistoryRepository.save(any<MedicalHistory>(), any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now(clock)))
    uiEvents.onNext(SummaryMedicalHistoryAnswerToggled(question, answer = newAnswer))

    val updatedMedicalHistory = medicalHistory.copy(
        diagnosedWithHypertension = if (question == DIAGNOSED_WITH_HYPERTENSION) newAnswer else UNKNOWN,
        isOnTreatmentForHypertension = if (question == IS_ON_TREATMENT_FOR_HYPERTENSION) newAnswer else UNKNOWN,
        hasHadHeartAttack = if (question == HAS_HAD_A_HEART_ATTACK) newAnswer else UNKNOWN,
        hasHadStroke = if (question == HAS_HAD_A_STROKE) newAnswer else UNKNOWN,
        hasHadKidneyDisease = if (question == HAS_HAD_A_KIDNEY_DISEASE) newAnswer else UNKNOWN,
        hasDiabetes = if (question == HAS_DIABETES) newAnswer else UNKNOWN)
    verify(medicalHistoryRepository).save(eq(updatedMedicalHistory), any())
  }

  private fun randomAnswer(): MedicalHistory.Answer {
    return MedicalHistory.Answer::class.java.enumConstants.asList().shuffled().first()
  }

  @Test
  fun `when blood pressure is clicked for editing, blood pressure update sheet should show up`() {
    val bloodPressureMeasurement = PatientMocker.bp()
    uiEvents.onNext(PatientSummaryBpClicked(bloodPressureMeasurement))

    verify(screen).showBloodPressureUpdateSheet(bloodPressureMeasurement.uuid)
  }

  @Suppress("Unused")
  private fun `params for editing blood pressures`(): List<List<Any>> {
    fun generateBps(bpEditableFor: Duration): List<List<Any>> {
      val durationAsMillis = bpEditableFor.toMillis()
      val bpCreatedAt = Instant.now(clock)

      return listOf(
          listOf(bpEditableFor, PatientMocker.bp(createdAt = bpCreatedAt.minusMillis(durationAsMillis + 1)), false),
          listOf(bpEditableFor, PatientMocker.bp(createdAt = bpCreatedAt.minusMillis(durationAsMillis * 2)), false),
          listOf(bpEditableFor, PatientMocker.bp(createdAt = bpCreatedAt.minusMillis((durationAsMillis * 0.5).toLong())), true),
          listOf(bpEditableFor, PatientMocker.bp(createdAt = bpCreatedAt.minusMillis(durationAsMillis)), true),
          listOf(bpEditableFor, PatientMocker.bp(createdAt = bpCreatedAt), true)
      )
    }

    return generateBps(Duration.ofMinutes(1L)) + generateBps(Duration.ofDays(2L))
  }

  @Parameters(method = "params for patient item changed")
  @Test
  fun `when anything is changed on the screen, assert that patient result preference is updated`(
      status: SyncStatus,
      screenCreated: Instant,
      hasChanged: Boolean
  ) {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, screenCreated))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItem(status)))

    if (hasChanged) {
      verify(patientSummaryResult).set(any())
    } else {
      verify(patientSummaryResult, never()).set(any())
    }

    verifyZeroInteractions(screen)
  }

  @Suppress("unused")
  fun `params for patient item changed`(): Array<Array<Any>> {
    return arrayOf(
        arrayOf("PENDING", Instant.now(clock), true),
        arrayOf("PENDING", Instant.now(clock).plus(1, ChronoUnit.MINUTES), false),
        arrayOf("DONE", Instant.now(clock), false),
        arrayOf("DONE", Instant.now(clock).plus(1, ChronoUnit.MINUTES), false)
    )
  }

  @Test
  fun `when an appointment is scheduled, patient result should be set`() {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, Instant.now(clock)))
    uiEvents.onNext(AppointmentScheduled)

    verify(patientSummaryResult).set(any())
    verifyZeroInteractions(screen)
  }

  @Test
  fun `when something is saved on summary screen and an appointment is scheduled, home screen should be called with scheduled result`() {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, Instant.now(clock)))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItem(SyncStatus.PENDING)))
    uiEvents.onNext(AppointmentScheduled)

    verify(patientSummaryResult, times(2)).set(any())
    verifyZeroInteractions(screen)
  }

  private fun patientSummaryItem(syncStatus: SyncStatus): PatientSummaryItems {
    val updatedAt = Instant.now(clock).plusMillis(10)
    return PatientSummaryItems(prescriptionItems = SummaryPrescribedDrugsItem(
        prescriptions = listOf(
            PatientMocker.prescription(syncStatus = syncStatus, updatedAt = updatedAt),
            PatientMocker.prescription(syncStatus = syncStatus, updatedAt = updatedAt))),
        bloodPressureListItems = listOf(SummaryBloodPressureListItem(
            measurement = PatientMocker.bp(syncStatus = syncStatus, updatedAt = updatedAt),
            daysAgo = Today,
            showDivider = true,
            formattedTime = updatedAt.toString(),
            addTopPadding = false
        )),
        medicalHistoryItems = SummaryMedicalHistoryItem(PatientMocker.medicalHistory(syncStatus = syncStatus, updatedAt = updatedAt), Today)
    )
  }

  @Test
  fun `when a new patient is registered, always show the status as saved`() {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.NEW_PATIENT, Instant.now(clock)))

    verify(patientSummaryResult, times(1)).set(PatientSummaryResult.Saved(patientUuid))
  }

  @Suppress("unused")
  fun medicalHistoryQuestionsAndAnswers(): List<List<Any>> {
    val questions = MedicalHistoryQuestion.values().asList()
    return questions
        .asSequence()
        .map { question -> listOf(question, randomAnswer()) }
        .toList()
  }

  @Parameters(method = "params for BP grouped by date")
  @Test
  fun `when BPs are grouped by dates, then only the last BP item in every group should show divider`(
      bloodPressureMeasurements: List<BloodPressureMeasurement>,
      expectedBloodPressureMeasurementItems: List<SummaryBloodPressureListItem>
  ) {
    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    whenever(bpRepository.newestMeasurementsForPatient(patientUuid, config.numberOfBpsToDisplay)).thenReturn(Observable.just(bloodPressureMeasurements))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(medicalHistory()))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT, screenCreatedTimestamp = Instant.now(clock)))

    verify(screen).populateList(
        prescribedDrugsItem = any(),
        measurementPlaceholderItems = any(),
        measurementItems = check {
          it.forEachIndexed { index, item ->
            assertThat(item.measurement).isEqualTo(expectedBloodPressureMeasurementItems[index].measurement)
            assertThat(item.showDivider).isEqualTo(expectedBloodPressureMeasurementItems[index].showDivider)
            assertThat(item.formattedTime).isEqualTo(expectedBloodPressureMeasurementItems[index].formattedTime)
          }
        },
        medicalHistoryItem = any()
    )
  }

  @Suppress("unused")
  private fun `params for BP grouped by date`(): List<List<Any>> {
    val bpsForTest1 = listOf(
        PatientMocker.bp(patientUuid = patientUuid, createdAt = Instant.now(clock).plusMillis(1000)),
        PatientMocker.bp(patientUuid = patientUuid, createdAt = Instant.now(clock).plusMillis(500)),
        PatientMocker.bp(patientUuid = patientUuid, createdAt = Instant.now(clock).minus(1, ChronoUnit.DAYS))
    )
    val bpsForTest2 = listOf(
        PatientMocker.bp(patientUuid = patientUuid, createdAt = Instant.now(clock)),
        PatientMocker.bp(patientUuid = patientUuid, createdAt = Instant.now(clock)),
        PatientMocker.bp(patientUuid = patientUuid, createdAt = Instant.now(clock).minus(1, ChronoUnit.DAYS)),
        PatientMocker.bp(patientUuid = patientUuid, createdAt = Instant.now(clock).minus(1, ChronoUnit.DAYS).plusMillis(1000)),
        PatientMocker.bp(patientUuid = patientUuid, createdAt = Instant.now(clock).minus(2, ChronoUnit.DAYS))
    )

    val displayTime = { instant: Instant ->
      instant.atZone(zoneId).format(timeFormatter)
    }

    return listOf(
        listOf<Any>(
            bpsForTest1,
            listOf(
                SummaryBloodPressureListItem(measurement = bpsForTest1[0], daysAgo = Today, showDivider = false, formattedTime = displayTime(bpsForTest1[0].createdAt), addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest1[1], daysAgo = Today, showDivider = true, formattedTime = displayTime(bpsForTest1[1].createdAt), addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest1[2], daysAgo = Today, showDivider = true, formattedTime = null, addTopPadding = false)
            )),
        listOf<Any>(
            bpsForTest2,
            listOf(
                SummaryBloodPressureListItem(measurement = bpsForTest2[0], daysAgo = Today, showDivider = false, formattedTime = displayTime(bpsForTest2[0].createdAt), addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest2[1], daysAgo = Today, showDivider = true, formattedTime = displayTime(bpsForTest2[1].createdAt), addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest2[2], daysAgo = Today, showDivider = false, formattedTime = displayTime(bpsForTest2[2].createdAt), addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest2[3], daysAgo = Today, showDivider = true, formattedTime = displayTime(bpsForTest2[3].createdAt), addTopPadding = false),
                SummaryBloodPressureListItem(measurement = bpsForTest2[4], daysAgo = Today, showDivider = true, formattedTime = null, addTopPadding = false)
            ))
    )
  }

  @Test
  @Parameters(method = "appointment cancelation reasons")
  fun `when patient's phone was marked as invalid after the phone number was last updated then update phone dialog should be shown`(
      cancelReason: AppointmentCancelReason
  ) {
    val canceledAppointment = PatientMocker.appointment(status = CANCELLED, cancelReason = cancelReason)
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).thenReturn(Observable.just(Just(canceledAppointment)))

    val phoneNumber = PatientMocker.phoneNumber(
        patientUuid = patientUuid,
        updatedAt = canceledAppointment.updatedAt - Duration.ofHours(2))
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(phoneNumber)))

    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.NEW_PATIENT, Instant.now(clock)))

    if (cancelReason == InvalidPhoneNumber) {
      verify(screen).showUpdatePhoneDialog(patientUuid)
    } else {
      verify(screen, never()).showUpdatePhoneDialog(patientUuid)
    }
  }

  @Test
  @Parameters(method = "appointment cancelation reasons")
  fun `when patient's phone was marked as invalid before the phone number was last updated then update phone dialog should not be shown`(
      cancelReason: AppointmentCancelReason
  ) {
    val canceledAppointment = PatientMocker.appointment(status = CANCELLED, cancelReason = cancelReason)
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).thenReturn(Observable.just(Just(canceledAppointment)))

    val phoneNumber = PatientMocker.phoneNumber(
        patientUuid = patientUuid,
        updatedAt = canceledAppointment.updatedAt + Duration.ofHours(2))
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(phoneNumber)))

    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.NEW_PATIENT, Instant.now(clock)))

    verify(screen, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Test
  @Parameters(method = "appointment cancelation reasons")
  fun `when update phone dialog feature is disabled then it should never be shown`(
      cancelReason: AppointmentCancelReason
  ) {
    val appointmentStream = Observable.just(
        None,
        Just(PatientMocker.appointment(cancelReason = null)),
        Just(PatientMocker.appointment(cancelReason = cancelReason)))
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).thenReturn(appointmentStream)

    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.NEW_PATIENT, Instant.now(clock)))

    verify(screen, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Suppress("unused")
  fun `appointment cancelation reasons`() = AppointmentCancelReason.values()

  @Test
  fun `when a canceled appointment with the patient does not exist then update phone dialog should not be shown`() {
    val appointmentStream = Observable.just(
        None,
        Just(PatientMocker.appointment(status = SCHEDULED, cancelReason = null)))
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).thenReturn(appointmentStream)

    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.NEW_PATIENT, Instant.now(clock)))

    verify(screen, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Test
  fun `when a new patient is missing a phone number, then avoid showing update phone dialog`() {
    whenever(appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)).thenReturn(Observable.just(None))
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(None))

    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        numberOfBpsToDisplay = 100)
    configSubject.onNext(config)

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.NEW_PATIENT, Instant.now(clock)))

    verify(screen, never()).showUpdatePhoneDialog(patientUuid)
  }

  @Test
  fun `when an existing patient is missing a phone number, a BP is recorded, and the user has never been reminded, then add phone dialog should be shown`() {
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(None))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).thenReturn(Single.just(false))
    whenever(missingPhoneReminderRepository.markReminderAsShownFor(patientUuid)).thenReturn(Completable.complete())

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, Instant.now(clock)))
    uiEvents.onNext(PatientSummaryBloodPressureSaved)

    verify(screen).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository).markReminderAsShownFor(patientUuid)
  }

  @Test
  fun `when an existing patient is missing a phone number, a BP hasn't been recorded yet, and the user has never been reminded, then add phone dialog should not be shown`() {
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(None))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).thenReturn(Single.just(false))
    whenever(missingPhoneReminderRepository.markReminderAsShownFor(patientUuid)).thenReturn(Completable.complete())

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, Instant.now(clock)))

    verify(screen, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  fun `when an existing patient is missing a phone number, and the user has been reminded before, then add phone dialog should not be shown`() {
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(None))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).thenReturn(Single.just(true))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, Instant.now(clock)))

    verify(screen, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  fun `when an existing patient has a phone number, then add phone dialog should not be shown`() {
    val phoneNumber = Just(PatientMocker.phoneNumber(number = "101"))
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(phoneNumber))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).thenReturn(Single.never())

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, Instant.now(clock)))

    verify(screen, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  fun `when a new patient has a phone number, then add phone dialog should not be shown`() {
    val phoneNumber = Just(PatientMocker.phoneNumber(number = "101"))
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(phoneNumber))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).thenReturn(Single.never())

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.NEW_PATIENT, Instant.now(clock)))

    verify(screen, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }

  @Test
  fun `when a new patient is missing a phone number, then add phone dialog should not be shown`() {
    whenever(patientRepository.phoneNumber(patientUuid)).thenReturn(Observable.just(None))
    whenever(missingPhoneReminderRepository.hasShownReminderFor(patientUuid)).thenReturn(Single.just(false))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.NEW_PATIENT, Instant.now(clock)))

    verify(screen, never()).showAddPhoneDialog(patientUuid)
    verify(missingPhoneReminderRepository, never()).markReminderAsShownFor(any())
  }
}
