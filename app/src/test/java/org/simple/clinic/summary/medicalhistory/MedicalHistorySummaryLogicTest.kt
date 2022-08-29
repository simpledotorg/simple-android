package org.simple.clinic.summary.medicalhistory

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithDiabetes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAHeartAttack
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAKidneyDisease
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAStroke
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnDiabetesTreatment
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnHypertensionTreatment
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.TestUtcClock
import org.simple.sharedTestCode.util.randomMedicalHistoryAnswer
import org.simple.sharedTestCode.uuid.FakeUuidGenerator
import java.time.Instant
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class MedicalHistorySummaryLogicTest {

  private val patientUuid = UUID.fromString("31665de6-0265-4e33-888f-526bdb274699")
  private val medicalHistory = TestData.medicalHistory(
      uuid = UUID.fromString("6182e51f-13b3-47d5-a479-bee127070814"),
      patientUuid = patientUuid,
      updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
      diagnosedWithHypertension = Unanswered,
      hasDiabetes = Unanswered,
      hasHadHeartAttack = Unanswered,
      hasHadKidneyDisease = Unanswered,
      hasHadStroke = Unanswered
  )
  private val facilityWithDiabetesManagementEnabled = TestData.facility(
      uuid = UUID.fromString("90bedaf8-5521-490e-b725-2b41839a83c7"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
  )
  private val facilityWithDiabetesManagementDisabled = TestData.facility(
      uuid = UUID.fromString("7c1708a2-585c-4e80-adaa-6544368a46c4"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
  )
  private val medicalHistoryUuid = UUID.fromString("5054c068-a4ae-4a1a-a5ff-ae0bf009f3cf")

  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val ui = mock<MedicalHistorySummaryUi>()
  private val clock = TestUtcClock()

  private val events = PublishSubject.create<UiEvent>()

  private lateinit var testFixture: MobiusTestFixture<MedicalHistorySummaryModel, MedicalHistorySummaryEvent, MedicalHistorySummaryEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `patient's medical history should be populated`() {
    // given
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    setupController()

    // then
    verify(ui).hideDiagnosisView()
    verify(ui).showDiabetesHistorySection()
    verify(ui).populateMedicalHistory(medicalHistory)
    verifyNoMoreInteractions(ui)
  }

  @Test
  @Parameters(method = "medicalHistoryQuestionsAndAnswers")
  fun `when answers for medical history questions are toggled, then the updated medical history should be saved`(
      question: MedicalHistoryQuestion,
      newAnswer: Answer
  ) {
    // given
    val medicalHistory = TestData.medicalHistory(
        diagnosedWithHypertension = Unanswered,
        hasHadHeartAttack = Unanswered,
        hasHadStroke = Unanswered,
        hasHadKidneyDisease = Unanswered,
        hasDiabetes = Unanswered,
        updatedAt = Instant.parse("2017-12-31T00:00:00Z")
    )
    val updatedMedicalHistory = medicalHistory.answered(question, newAnswer)
    val now = Instant.now(clock)

    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    setupController()
    events.onNext(SummaryMedicalHistoryAnswerToggled(question, answer = newAnswer))

    // then
    verify(medicalHistoryRepository).save(updatedMedicalHistory, now)
  }

  @Test
  fun `when the current facility supports diabetes management, show the diagnosis view and hide the diabetes history question`() {
    // given
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    setupController(facility = facilityWithDiabetesManagementEnabled)

    // then
    verify(ui).populateMedicalHistory(medicalHistory)
    verify(ui).showDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the current facility does not support diabetes management, hide the diagnosis view and show the diabetes history question`() {
    // given
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    setupController()

    // then
    verify(ui).populateMedicalHistory(medicalHistory)
    verify(ui).hideDiagnosisView()
    verify(ui).showDiabetesHistorySection()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the diagnosed with hypertension answer is changed, clear the diagnosis error`() {
    // given
    val updatedMedicalHistory = medicalHistory.answered(DiagnosedWithHypertension, Yes)

    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    setupController(facility = facilityWithDiabetesManagementEnabled)
    events.onNext(SummaryMedicalHistoryAnswerToggled(DiagnosedWithHypertension, Yes))

    // then
    verify(ui).populateMedicalHistory(medicalHistory)
    verify(ui).populateMedicalHistory(updatedMedicalHistory)
    verify(ui).showDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the diagnosed with diabetes answer is changed, clear the diagnosis error`() {
    // given
    val updatedMedicalHistory = medicalHistory.answered(DiagnosedWithDiabetes, No)

    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    setupController(facility = facilityWithDiabetesManagementEnabled)
    events.onNext(SummaryMedicalHistoryAnswerToggled(DiagnosedWithDiabetes, No))

    // then
    verify(ui).populateMedicalHistory(medicalHistory)
    verify(ui).populateMedicalHistory(updatedMedicalHistory)
    verify(ui).showDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the has had kidney disease answer is changed, do not clear the diagnosis error`() {
    // given
    val updatedMedicalHistory = medicalHistory.answered(HasHadAKidneyDisease, Yes)

    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    setupController(facility = facilityWithDiabetesManagementEnabled)
    events.onNext(SummaryMedicalHistoryAnswerToggled(HasHadAKidneyDisease, Yes))

    // then
    verify(ui).populateMedicalHistory(medicalHistory)
    verify(ui).populateMedicalHistory(updatedMedicalHistory)
    verify(ui).showDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the has had heart attack answer is changed, do not clear the diagnosis error`() {
    // given
    val updatedMedicalHistory = medicalHistory.answered(HasHadAHeartAttack, No)

    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    setupController(facility = facilityWithDiabetesManagementEnabled)
    events.onNext(SummaryMedicalHistoryAnswerToggled(HasHadAHeartAttack, No))

    // then
    verify(ui).populateMedicalHistory(medicalHistory)
    verify(ui).populateMedicalHistory(updatedMedicalHistory)
    verify(ui).showDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the has had a stroke answer is changed, do not clear the diagnosis error`() {
    // given
    val updatedMedicalHistory = medicalHistory.answered(HasHadAStroke, Yes)

    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    setupController(facility = facilityWithDiabetesManagementEnabled)
    events.onNext(SummaryMedicalHistoryAnswerToggled(HasHadAStroke, Yes))

    // then
    verify(ui).populateMedicalHistory(medicalHistory)
    verify(ui).populateMedicalHistory(updatedMedicalHistory)
    verify(ui).showDiagnosisView()
    verify(ui).hideDiabetesHistorySection()
    verifyNoMoreInteractions(ui)
  }

  @Suppress("unused")
  fun medicalHistoryQuestionsAndAnswers(): List<List<Any>> {
    val questions = listOf(
        DiagnosedWithHypertension,
        IsOnHypertensionTreatment(Country.INDIA),
        DiagnosedWithDiabetes,
        IsOnDiabetesTreatment,
        HasHadAHeartAttack,
        HasHadAStroke,
        HasHadAKidneyDisease
    )
    return questions
        .map { question -> listOf(question, randomMedicalHistoryAnswer()) }
        .toList()
  }

  private fun setupController(facility: Facility = facilityWithDiabetesManagementDisabled) {
    val effectHandler = MedicalHistorySummaryEffectHandler(
        schedulers = TrampolineSchedulersProvider(),
        medicalHistoryRepository = medicalHistoryRepository,
        clock = clock,
        currentFacility = Lazy { facility },
        uuidGenerator = FakeUuidGenerator.fixed(medicalHistoryUuid)
    )

    val uiRenderer = MedicalHistorySummaryUiRenderer(ui)
    testFixture = MobiusTestFixture(
        events = events.ofType(),
        defaultModel = MedicalHistorySummaryModel.create(patientUuid),
        init = MedicalHistorySummaryInit(),
        update = MedicalHistorySummaryUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
