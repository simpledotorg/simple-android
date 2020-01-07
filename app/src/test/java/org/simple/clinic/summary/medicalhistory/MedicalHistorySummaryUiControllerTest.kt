package org.simple.clinic.summary.medicalhistory

import com.nhaarman.mockito_kotlin.doReturn
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
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.summary.PatientSummaryScreenUi
import org.simple.clinic.util.TestUtcClock
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
  private val clock = TestUtcClock()

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

  @Test
  @Parameters(method = "medicalHistoryQuestionsAndAnswers")
  fun `when answers for medical history questions are toggled, then the updated medical history should be saved`(
      question: MedicalHistoryQuestion,
      newAnswer: Answer
  ) {
    // given
    val medicalHistory = PatientMocker.medicalHistory(
        diagnosedWithHypertension = Answer.Unanswered,
        isOnTreatmentForHypertension = Answer.Unanswered,
        hasHadHeartAttack = Answer.Unanswered,
        hasHadStroke = Answer.Unanswered,
        hasHadKidneyDisease = Answer.Unanswered,
        hasDiabetes = Answer.Unanswered,
        updatedAt = Instant.parse("2017-12-31T00:00:00Z")
    )
    val updatedMedicalHistory = medicalHistory.answered(question, newAnswer)
    val now = Instant.now(clock)

    whenever(repository.historyForPatientOrDefault(patientUuid)) doReturn Observable.just(medicalHistory)
    whenever(repository.save(updatedMedicalHistory, now)) doReturn Completable.complete()

    // when
    setupController()
    events.onNext(SummaryMedicalHistoryAnswerToggled(question, answer = newAnswer))

    // then
    verify(repository).save(updatedMedicalHistory, now)
  }

  @Suppress("unused")
  fun medicalHistoryQuestionsAndAnswers(): List<List<Any>> {
    val questions = MedicalHistoryQuestion.values().asList()
    return questions
        .map { question -> listOf(question, randomMedicalHistoryAnswer()) }
        .toList()
  }

  private fun setupController() {
    controller = MedicalHistorySummaryUiController(patientUuid, repository, clock)
    controllerSubscription = events.compose(controller).subscribe { it.invoke(ui) }

    events.onNext(ScreenCreated())
  }
}
