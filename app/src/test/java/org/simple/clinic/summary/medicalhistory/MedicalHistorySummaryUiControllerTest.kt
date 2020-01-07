package org.simple.clinic.summary.medicalhistory

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.summary.PatientSummaryScreenUi
import org.simple.clinic.util.randomMedicalHistoryAnswer
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class MedicalHistorySummaryUiControllerTest {

  private val patientUuid = UUID.fromString("31665de6-0265-4e33-888f-526bdb274699")

  private val repository = mock<MedicalHistoryRepository>()
  private val ui = mock<PatientSummaryScreenUi>()
  private val medicalHistorySummaryUi = mock<MedicalHistorySummaryUi>()

  private val events = PublishSubject.create<UiEvent>()

  lateinit var controller: MedicalHistorySummaryUiController
  lateinit var controllerSubscription: Disposable

  @Before
  fun setUp() {
    whenever(ui.medicalHistorySummaryUi()) doReturn medicalHistorySummaryUi
  }

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `patient's medical history should be populated`() {
    // given
    val medicalHistory = PatientMocker.medicalHistory(updatedAt = Instant.parse("2018-01-01T00:00:00Z"))
    whenever(repository.historyForPatientOrDefault(patientUuid)) doReturn Observable.just(medicalHistory)

    // when
    setupController()

    // then
    verify(medicalHistorySummaryUi).populateMedicalHistory(medicalHistory)
    verifyNoMoreInteractions(medicalHistorySummaryUi)
  }

  // This was copied from `PatientSummaryScreenControllerTest`
  // TODO(vs): 2020-01-07 Clean this up once the refactoring is done
  @Test
  @Parameters(method = "medicalHistoryQuestionsAndAnswers")
  fun `when answers for medical history questions are toggled, then the updated medical history should be saved`(
      question: MedicalHistoryQuestion,
      newAnswer: Answer
  ) {
    val medicalHistory = PatientMocker.medicalHistory(
        diagnosedWithHypertension = Answer.Unanswered,
        isOnTreatmentForHypertension = Answer.Unanswered,
        hasHadHeartAttack = Answer.Unanswered,
        hasHadStroke = Answer.Unanswered,
        hasHadKidneyDisease = Answer.Unanswered,
        hasDiabetes = Answer.Unanswered,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    whenever(repository.historyForPatientOrDefault(patientUuid)) doReturn Observable.just(medicalHistory)
    whenever(repository.save(any<MedicalHistory>(), any())) doReturn Completable.complete()

    setupController()
    events.onNext(SummaryMedicalHistoryAnswerToggled(question, answer = newAnswer))

    val updatedMedicalHistory = medicalHistory.copy(
        diagnosedWithHypertension = if (question == MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION) newAnswer else Answer.Unanswered,
        isOnTreatmentForHypertension = if (question == MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION) newAnswer else Answer.Unanswered,
        hasHadHeartAttack = if (question == MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK) newAnswer else Answer.Unanswered,
        hasHadStroke = if (question == MedicalHistoryQuestion.HAS_HAD_A_STROKE) newAnswer else Answer.Unanswered,
        hasHadKidneyDisease = if (question == MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE) newAnswer else Answer.Unanswered,
        hasDiabetes = if (question == MedicalHistoryQuestion.HAS_DIABETES) newAnswer else Answer.Unanswered)
    verify(repository).save(eq(updatedMedicalHistory), any())
  }

  @Suppress("unused")
  fun medicalHistoryQuestionsAndAnswers(): List<List<Any>> {
    val questions = MedicalHistoryQuestion.values().asList()
    return questions
        .asSequence()
        .map { question ->
          listOf(
              question,
              randomMedicalHistoryAnswer()
          )
        }
        .toList()
  }

  private fun setupController() {
    controller = MedicalHistorySummaryUiController(patientUuid, repository)
    controllerSubscription = events.compose(controller).subscribe { it.invoke(ui) }

    events.onNext(ScreenCreated())
  }
}
