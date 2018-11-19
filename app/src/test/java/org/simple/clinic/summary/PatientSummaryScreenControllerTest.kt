package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSummaryResult
import org.simple.clinic.patient.PatientSummaryResult.NotSaved
import org.simple.clinic.patient.PatientSummaryResult.Saved
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSummaryScreenControllerTest {

  private val screen = mock<PatientSummaryScreen>()
  private val patientRepository = mock<PatientRepository>()
  private val bpRepository = mock<BloodPressureRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val patientUuid = UUID.randomUUID()
  private val clock = Clock.fixed(Instant.EPOCH, UTC)

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val configSubject = BehaviorSubject.create<PatientSummaryConfig>()
  private val reporter = MockAnalyticsReporter()

  private lateinit var controller: PatientSummaryScreenController

  @Before
  fun setUp() {
    val timestampGenerator = RelativeTimestampGenerator()

    controller = PatientSummaryScreenController(
        patientRepository,
        bpRepository,
        prescriptionRepository,
        medicalHistoryRepository,
        timestampGenerator,
        clock,
        configSubject.firstOrError())

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    whenever(patientRepository.patient(patientUuid)).thenReturn(Observable.never())
    whenever(patientRepository.phoneNumbers(patientUuid)).thenReturn(Observable.never())
    whenever(bpRepository.newest100MeasurementsForPatient(patientUuid)).thenReturn(Observable.never())
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.never())
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.never())

    Analytics.addReporter(reporter)
  }

  @Test
  fun `patient's profile should be populated`() {
    val addressUuid = UUID.randomUUID()
    val patient = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid)
    val address = PatientMocker.address(uuid = addressUuid)
    val phoneNumber = None

    whenever(patientRepository.patient(patientUuid)).thenReturn(Observable.just(Just(patient)))
    whenever(patientRepository.address(addressUuid)).thenReturn(Observable.just(Just(address)))
    whenever(patientRepository.phoneNumbers(patientUuid)).thenReturn(Observable.just(phoneNumber))
    whenever(bpRepository.newest100MeasurementsForPatient(patientUuid)).thenReturn(Observable.never())

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT, screenCreatedTimestamp = Instant.now()))

    verify(screen).populatePatientProfile(patient, address, phoneNumber)
  }

  @Test
  fun `patient's prescription summary should be populated`() {
    val config = PatientSummaryConfig(numberOfBpPlaceholders = 0, bpEditableFor = Duration.ofSeconds(30L), isPatientEditFeatureEnabled = false)
    configSubject.onNext(config)

    val prescriptions = listOf(
        PatientMocker.prescription(name = "Amlodipine", dosage = "10mg"),
        PatientMocker.prescription(name = "Telmisartan", dosage = "9000mg"),
        PatientMocker.prescription(name = "Randomzole", dosage = "2 packets"))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(prescriptions))
    whenever(bpRepository.newest100MeasurementsForPatient(patientUuid)).thenReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(PatientMocker.medicalHistory()))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now()))

    verify(screen).populateList(eq(SummaryPrescribedDrugsItem(prescriptions)), any(), any(), any())
  }

  @Test
  fun `patient's blood pressure history should be populated`() {
    val config = PatientSummaryConfig(numberOfBpPlaceholders = 0, bpEditableFor = Duration.ofSeconds(30L), isPatientEditFeatureEnabled = false)
    configSubject.onNext(config)

    val bloodPressureMeasurements = listOf(
        PatientMocker.bp(patientUuid, systolic = 120, diastolic = 85, createdAt = Instant.now(clock).minusSeconds(15L)),
        PatientMocker.bp(patientUuid, systolic = 164, diastolic = 95, createdAt = Instant.now(clock).minusSeconds(30L)),
        PatientMocker.bp(patientUuid, systolic = 144, diastolic = 90, createdAt = Instant.now(clock).minusSeconds(45L)))

    whenever(bpRepository.newest100MeasurementsForPatient(patientUuid)).thenReturn(Observable.just(bloodPressureMeasurements))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(PatientMocker.medicalHistory()))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT, screenCreatedTimestamp = Instant.now()))

    verify(screen).populateList(
        any(),
        any(),
        check {
          it.forEachIndexed { i, item -> assertThat(item.measurement).isEqualTo(bloodPressureMeasurements[i]) }
          assertThat(it[0].isEditable).isTrue()
          assertThat(it[1].isEditable).isTrue()
          assertThat(it[2].isEditable).isFalse()
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
    val config = PatientSummaryConfig(numberOfBpPlaceholders = 3, bpEditableFor = Duration.ofSeconds(30L), isPatientEditFeatureEnabled = false)
    configSubject.onNext(config)

    whenever(bpRepository.newest100MeasurementsForPatient(patientUuid)).thenReturn(Observable.just(bloodPressureMeasurements))
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(emptyList()))
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(PatientMocker.medicalHistory()))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT, screenCreatedTimestamp = Instant.now()))

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

    // We won't be verifying the relative timestamps in the test this is used in,
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
                SummaryBloodPressureListItem(measurement = bpsForTest2[0], timestamp = Today)
            )
        ),
        listOf<Any>(
            bpsForTest3,
            listOf(SummaryBloodPressurePlaceholderListItem(1)),
            listOf(
                SummaryBloodPressureListItem(measurement = bpsForTest3[0], timestamp = Today),
                SummaryBloodPressureListItem(measurement = bpsForTest3[1], timestamp = Today)
            )
        ),
        listOf<Any>(
            bpsForTest4,
            emptyList<SummaryBloodPressurePlaceholderListItem>(),
            listOf(
                SummaryBloodPressureListItem(measurement = bpsForTest4[0], timestamp = Today),
                SummaryBloodPressureListItem(measurement = bpsForTest4[1], timestamp = Today),
                SummaryBloodPressureListItem(measurement = bpsForTest4[2], timestamp = Today)
            )
        )
    )
  }

  @Test
  fun `patient's medical history should be populated`() {
    val config = PatientSummaryConfig(numberOfBpPlaceholders = 0, bpEditableFor = Duration.ofSeconds(30L), isPatientEditFeatureEnabled = false)
    configSubject.onNext(config)

    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)).thenReturn(Observable.just(emptyList()))
    whenever(bpRepository.newest100MeasurementsForPatient(patientUuid)).thenReturn(Observable.just(emptyList()))

    val medicalHistory = PatientMocker.medicalHistory(updatedAt = Instant.now())
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(medicalHistory))

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now()))

    verify(screen).populateList(any(), any(), any(), eq(SummaryMedicalHistoryItem(medicalHistory, Today)))
  }

  @Test
  fun `when new-BP is clicked then BP entry sheet should be shown`() {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now()))
    uiEvents.onNext(PatientSummaryNewBpClicked())

    verify(screen, times(1)).showBloodPressureEntrySheet(patientUuid)
    verify(screen, never()).showBloodPressureEntrySheetIfNotShownAlready(any())
  }

  @Test
  fun `when screen was opened after saving a new patient then BP entry sheet should be shown`() {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now()))
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.NEW_PATIENT, screenCreatedTimestamp = Instant.now()))

    verify(screen, times(1)).showBloodPressureEntrySheetIfNotShownAlready(any())
    verify(screen, never()).showBloodPressureEntrySheet(any())
  }

  @Test
  @Parameters(method = "bpSavedAndPatientSummaryCallers")
  fun `when back is clicked, then user should be taken back to search, or schedule appointment sheet should open`(
      wasBloodPressureSaved: Boolean,
      patientSummaryCaller: PatientSummaryCaller
  ) {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = patientSummaryCaller, screenCreatedTimestamp = Instant.now()))
    uiEvents.onNext(PatientSummaryBloodPressureClosed(wasBloodPressureSaved))
    uiEvents.onNext(PatientSummaryBackClicked())

    if (wasBloodPressureSaved) {
      verify(screen).showScheduleAppointmentSheet(patientUuid)
    } else {
      if (patientSummaryCaller == PatientSummaryCaller.NEW_PATIENT) {
        verify(screen).goBackToHome(NotSaved)
      } else {
        verify(screen).goBackToPatientSearch()
      }
    }
  }

  @Test
  @Parameters(method = "bpSavedAndPatientSummaryCallers")
  fun `when save button is clicked, then user should be taken back to the home screen, or schedule appointment sheet should open`(
      wasBloodPressureSaved: Boolean,
      patientSummaryCaller: PatientSummaryCaller
  ) {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = patientSummaryCaller, screenCreatedTimestamp = Instant.now()))
    uiEvents.onNext(PatientSummaryBloodPressureClosed(wasBloodPressureSaved))
    uiEvents.onNext(PatientSummaryResultSet(Saved(patientUuid)))
    uiEvents.onNext(PatientSummaryDoneClicked())

    if (wasBloodPressureSaved) {
      verify(screen).showScheduleAppointmentSheet(patientUuid)
      verify(screen, never()).goBackToHome(Saved(patientUuid))
    } else {
      verify(screen).goBackToHome(Saved(patientUuid))
      verify(screen, never()).showScheduleAppointmentSheet(any())
    }
  }

  @Test
  @Parameters(method = "bpSavedAndPatientSummaryCallers")
  fun `when summary screen is restored, and bp was saved earlier, schedule appointment sheet should open, on clicking back or done`(
      wasBloodPressureSaved: Boolean,
      patientSummaryCaller: PatientSummaryCaller
  ) {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = patientSummaryCaller, screenCreatedTimestamp = Instant.now()))
    uiEvents.onNext(PatientSummaryRestoredWithBPSaved(wasBloodPressureSaved))
    uiEvents.onNext(PatientSummaryDoneClicked())

    if (wasBloodPressureSaved) {
      verify(screen).showScheduleAppointmentSheet(patientUuid)
      verify(screen, never()).goBackToHome(any())
    } else {
      verify(screen).goBackToHome(NotSaved)
      verify(screen, never()).showScheduleAppointmentSheet(any())
    }
  }

  @Suppress("unused")
  fun bpSavedAndPatientSummaryCallers() = arrayOf(
      arrayOf(true, PatientSummaryCaller.NEW_PATIENT),
      arrayOf(true, PatientSummaryCaller.SEARCH),
      arrayOf(false, PatientSummaryCaller.NEW_PATIENT),
      arrayOf(false, PatientSummaryCaller.SEARCH)
  )

  @Test
  fun `when update medicines is clicked then BP medicines screen should be shown`() {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now()))
    uiEvents.onNext(PatientSummaryUpdateDrugsClicked())

    verify(screen).showUpdatePrescribedDrugsScreen(patientUuid)
  }

  @Test
  @Parameters(
      "SEARCH",
      "NEW_PATIENT"
  )
  fun `when the screen is opened, the viewed patient analytics event must be sent`(fromCaller: PatientSummaryCaller) {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, fromCaller, Instant.now()))

    val expectedEvent = MockAnalyticsReporter.Event("ViewedPatient", mapOf(
        "patientId" to patientUuid.toString(),
        "from" to fromCaller.name
    ))
    assertThat(reporter.receivedEvents).contains(expectedEvent)
  }

  @Test
  @Parameters(method = "medicalHistoryQuestionsWithoutNone")
  fun `when answers for medical history questions are toggled, then the updated medical history should be saved`(
      question: MedicalHistoryQuestion
  ) {
    val medicalHistory = PatientMocker.medicalHistory(
        diagnosedWithHypertension = false,
        isOnTreatmentForHypertension = false,
        hasHadHeartAttack = false,
        hasHadStroke = false,
        hasHadKidneyDisease = false,
        hasDiabetes = false,
        updatedAt = Instant.now())
    whenever(medicalHistoryRepository.historyForPatientOrDefault(patientUuid)).thenReturn(Observable.just(medicalHistory))
    whenever(medicalHistoryRepository.save(any<MedicalHistory>(), any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, caller = PatientSummaryCaller.SEARCH, screenCreatedTimestamp = Instant.now()))
    uiEvents.onNext(SummaryMedicalHistoryAnswerToggled(question, selected = true))

    val updatedMedicalHistory = medicalHistory.copy(
        diagnosedWithHypertension = question == DIAGNOSED_WITH_HYPERTENSION,
        isOnTreatmentForHypertension = question == IS_ON_TREATMENT_FOR_HYPERTENSION,
        hasHadHeartAttack = question == HAS_HAD_A_HEART_ATTACK,
        hasHadStroke = question == HAS_HAD_A_STROKE,
        hasHadKidneyDisease = question == HAS_HAD_A_KIDNEY_DISEASE,
        hasDiabetes = question == HAS_DIABETES)
    verify(medicalHistoryRepository).save(eq(updatedMedicalHistory), any())
  }

  @Test
  @Parameters(method = "params for editing blood pressures")
  fun `when blood pressure is clicked for editing, only those recorded in a given period must be editable`(
      bpEditableFor: Duration,
      bloodPressureMeasurement: BloodPressureMeasurement,
      shouldBeEditable: Boolean
  ) {
    val config = PatientSummaryConfig(numberOfBpPlaceholders = 0, bpEditableFor = bpEditableFor, isPatientEditFeatureEnabled = false)
    configSubject.onNext(config)

    uiEvents.onNext(PatientSummaryBpClicked(bloodPressureMeasurement))

    if (shouldBeEditable) {
      verify(screen).showBloodPressureUpdateSheet(bloodPressureMeasurement.uuid)
    } else {
      verify(screen, never()).showBloodPressureUpdateSheet(any())
    }
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

  @Test
  @Parameters(value = [
    "true",
    "false"
  ])
  fun `when the edit patient feature flag is unset, the feature must be disabled on the screen`(isPatientEditFeatureEnabled: Boolean) {
    val config = PatientSummaryConfig(
        numberOfBpPlaceholders = 0,
        bpEditableFor = Duration.ofSeconds(0L),
        isPatientEditFeatureEnabled = isPatientEditFeatureEnabled
    )

    configSubject.onNext(config)
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, Instant.now()))

    if (isPatientEditFeatureEnabled) {
      verify(screen, never()).disableEditPatientFeature()
      verify(screen).enableEditPatientFeature()
    } else {
      verify(screen, never()).enableEditPatientFeature()
      verify(screen).disableEditPatientFeature()
    }
  }

  @Parameters(method = "params for patient item changed")
  @Test
  fun `when anything is changed on the screen and save or back is clicked, home screen should be called with correct result`(
      status: SyncStatus,
      screenCreated: Instant,
      hasChanged: Boolean
  ) {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, screenCreated))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItem(status)))
    uiEvents.onNext(PatientSummaryDoneClicked())

    if (hasChanged) {
      verify(screen).goBackToHome(Saved(patientUuid))
    } else {
      verify(screen).goBackToHome(NotSaved)
    }
  }


  @Test
  fun `when an appointment is scheduled and save or back is clicked, home screen should be called with scheduled result`() {
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.NEW_PATIENT, Instant.now()))
    uiEvents.onNext(PatientSummaryDoneClicked())
    uiEvents.onNext(AppointmentScheduled())
    uiEvents.onNext(ScheduleAppointmentSheetClosed())

    verify(screen).goBackToHome(PatientSummaryResult.Scheduled(patientUuid))
  }

  @Test
  fun `when something is saved on summary screen and an appointment is scheduled, home screen should be called with scheduled result`(){
    uiEvents.onNext(PatientSummaryScreenCreated(patientUuid, PatientSummaryCaller.SEARCH, Instant.now(clock)))
    uiEvents.onNext(PatientSummaryItemChanged(patientSummaryItem(SyncStatus.PENDING)))
    uiEvents.onNext(PatientSummaryDoneClicked())
    uiEvents.onNext(AppointmentScheduled)
    uiEvents.onNext(ScheduleAppointmentSheetClosed())

    verify(screen).goBackToHome(Scheduled(patientUuid))
  }

  private fun patientSummaryItem(syncStatus: SyncStatus): PatientSummaryItems {
    return PatientSummaryItems(prescriptionItems = SummaryPrescribedDrugsItem(
        prescriptions = listOf(
            PatientMocker.prescription(syncStatus = syncStatus),
            PatientMocker.prescription(syncStatus = syncStatus))),
        bloodPressureListItems = listOf(SummaryBloodPressureListItem(PatientMocker.bp(syncStatus = syncStatus), Today, false)
        ),
        medicalHistoryItems = SummaryMedicalHistoryItem(PatientMocker.medicalHistory(syncStatus = syncStatus), Today)
    )
  }

  fun `params for patient item changed`(): Array<Array<Any>> {
    return arrayOf(
        arrayOf("PENDING", Instant.now(), true),
        arrayOf("PENDING", Instant.now().plus(1, ChronoUnit.MINUTES), false),
        arrayOf("DONE", Instant.now(), false),
        arrayOf("DONE", Instant.now().plus(1, ChronoUnit.MINUTES), false)
    )
  }

  @Suppress("unused")
  fun medicalHistoryQuestionsWithoutNone() = MedicalHistoryQuestion.values().filter { it != MedicalHistoryQuestion.NONE }

  @After
  fun tearDown() {
    Analytics.clearReporters()
    reporter.clear()
  }
}
