package org.simple.clinic.medicalhistory.newentry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.NONE
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class NewMedicalHistoryScreenControllerTest {

  private val screen: NewMedicalHistoryScreen = mock()
  private val medicalHistoryRepository: MedicalHistoryRepository = mock()
  private val patientRepository: PatientRepository = mock()

  private lateinit var controller: NewMedicalHistoryScreenController
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    controller = NewMedicalHistoryScreenController(medicalHistoryRepository, patientRepository)

    whenever(medicalHistoryRepository.save(any(), any())).thenReturn(Completable.complete())

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when any answer is selected except for none then none should be selected`() {
    val answersMinusNone = MedicalHistoryQuestion.values().filter { it != NONE }

    answersMinusNone.forEach {
      uiEvents.onNext(NewMedicalHistoryAnswerToggled(question = it, selected = false))
      uiEvents.onNext(NewMedicalHistoryAnswerToggled(question = it, selected = true))
    }

    verify(screen, times(answersMinusNone.size)).unSelectNoneAnswer()
  }

  @Test
  fun `when none is selected then all answers should get unselected`() {
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(question = NONE, selected = true))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(question = NONE, selected = false))
    verify(screen).unSelectAllAnswersExceptNone()
  }

  @Test
  fun `when save is clicked with selected answers then patient with the answers should be saved and summary screen should be opened`() {
    val savedPatient = PatientMocker.patient(uuid = UUID.randomUUID())
    whenever(patientRepository.saveOngoingEntryAsPatient()).thenReturn(Single.just(savedPatient))

    val answersMinusNone = MedicalHistoryQuestion.values().filter { it != NONE }
    val selectedAnswers = answersMinusNone.shuffled().subList(0, answersMinusNone.size / 2)
    val unselectedAnswers = answersMinusNone.minus(selectedAnswers)

    uiEvents.onNext(ScreenCreated())
    selectedAnswers.forEach {
      uiEvents.onNext(NewMedicalHistoryAnswerToggled(it, selected = true))
    }
    unselectedAnswers.forEach {
      uiEvents.onNext(NewMedicalHistoryAnswerToggled(it, selected = false))
    }
    uiEvents.onNext(SaveMedicalHistoryClicked())

    val inOrder = inOrder(medicalHistoryRepository, patientRepository, screen)
    inOrder.verify(patientRepository).saveOngoingEntryAsPatient()
    inOrder.verify(medicalHistoryRepository).save(
        patientUuid = savedPatient.uuid,
        historyEntry = OngoingMedicalHistoryEntry(
            hasHadHeartAttack = selectedAnswers.contains(HAS_HAD_A_HEART_ATTACK),
            hasHadStroke = selectedAnswers.contains(HAS_HAD_A_STROKE),
            hasHadKidneyDisease = selectedAnswers.contains(HAS_HAD_A_KIDNEY_DISEASE),
            isOnTreatmentForHypertension = selectedAnswers.contains(IS_ON_TREATMENT_FOR_HYPERTENSION),
            hasDiabetes = selectedAnswers.contains(HAS_DIABETES)))
    inOrder.verify(screen).openPatientSummaryScreen(savedPatient.uuid)
  }

  @Test
  fun `when save is clicked with no answers then patient with an empty medical history should be saved and summary screen should be opened`() {
    val savedPatient = PatientMocker.patient(uuid = UUID.randomUUID())
    whenever(patientRepository.saveOngoingEntryAsPatient()).thenReturn(Single.just(savedPatient))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(question = NONE, selected = true))
    uiEvents.onNext(SaveMedicalHistoryClicked())

    val inOrder = inOrder(medicalHistoryRepository, patientRepository, screen)
    inOrder.verify(patientRepository).saveOngoingEntryAsPatient()
    inOrder.verify(medicalHistoryRepository).save(
        patientUuid = savedPatient.uuid,
        historyEntry = OngoingMedicalHistoryEntry(
            hasHadHeartAttack = false,
            hasHadStroke = false,
            hasHadKidneyDisease = false,
            isOnTreatmentForHypertension = false,
            hasDiabetes = false))
    inOrder.verify(screen).openPatientSummaryScreen(savedPatient.uuid)
  }

  @Test
  fun `save button should remain enabled only when atleast one answer is selected`() {
    val answers = MedicalHistoryQuestion.values()
    answers.forEach {
      uiEvents.onNext(NewMedicalHistoryAnswerToggled(question = it, selected = false))
    }
    answers.forEach {
      uiEvents.onNext(NewMedicalHistoryAnswerToggled(question = it, selected = true))
      uiEvents.onNext(NewMedicalHistoryAnswerToggled(question = it, selected = false))
    }
    verify(screen, times(answers.size)).setSaveButtonEnabled(true)
    verify(screen, times(answers.size + 1)).setSaveButtonEnabled(false)
  }
}
