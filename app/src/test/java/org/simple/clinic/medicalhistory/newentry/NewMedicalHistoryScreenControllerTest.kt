package org.simple.clinic.medicalhistory.newentry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.randomMedicalHistoryAnswer
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class NewMedicalHistoryScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: NewMedicalHistoryUi = mock()
  private val medicalHistoryRepository: MedicalHistoryRepository = mock()
  private val facilityRepository = mock<FacilityRepository>()
  private val patientRepository: PatientRepository = mock()
  private val userSession = mock<UserSession>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val user = PatientMocker.loggedInUser(uuid = UUID.fromString("4eb3d692-7362-4b10-848a-a7d679aee23a"))
  private val facility = PatientMocker.facility(uuid = UUID.fromString("6fc07446-c508-47e7-998e-8c475f9114d1"))
  private val patientUuid = UUID.fromString("d4f0fb3a-0146-4bc6-afec-95b76c61edca")

  @Before
  fun setUp() {
    whenever(medicalHistoryRepository.save(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.never())
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))
    whenever(facilityRepository.currentFacility(user)).thenReturn(Observable.just(facility))

    setupController()
  }

  @Test
  fun `when screen is started then the patient's name should be shown on the toolbar`() {
    val patientName = "Ashok Kumar"
    val patientEntry = OngoingNewPatientEntry(personalDetails = PersonalDetails(
        fullName = patientName,
        dateOfBirth = null,
        age = "20",
        gender = Gender.Transgender))
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(patientEntry))

    uiEvents.onNext(ScreenCreated())

    verify(screen).setPatientName(patientName)
  }

  @Test
  fun `when save is clicked with selected answers then patient with the answers should be saved and summary screen should be opened`() {
    val savedPatient = PatientMocker.patient(uuid = patientUuid)
    whenever(patientRepository.saveOngoingEntryAsPatient(user, facility)).thenReturn(Single.just(savedPatient))

    val questionsAndAnswers = MedicalHistoryQuestion.values()
        .map { it to randomMedicalHistoryAnswer() }
        .toMap()

    uiEvents.onNext(ScreenCreated())
    questionsAndAnswers.forEach { (question, answer) ->
      uiEvents.onNext(NewMedicalHistoryAnswerToggled(question, answer))
    }
    uiEvents.onNext(SaveMedicalHistoryClicked())

    val inOrder = inOrder(medicalHistoryRepository, patientRepository, screen)
    inOrder.verify(patientRepository).saveOngoingEntryAsPatient(user, facility)
    inOrder.verify(medicalHistoryRepository).save(
        patientUuid = savedPatient.uuid,
        historyEntry = OngoingMedicalHistoryEntry(
            diagnosedWithHypertension = questionsAndAnswers[DIAGNOSED_WITH_HYPERTENSION]!!,
            isOnTreatmentForHypertension = questionsAndAnswers[IS_ON_TREATMENT_FOR_HYPERTENSION]!!,
            hasHadHeartAttack = questionsAndAnswers[HAS_HAD_A_HEART_ATTACK]!!,
            hasHadStroke = questionsAndAnswers[HAS_HAD_A_STROKE]!!,
            hasHadKidneyDisease = questionsAndAnswers[HAS_HAD_A_KIDNEY_DISEASE]!!,
            hasDiabetes = questionsAndAnswers[HAS_DIABETES]!!)
    )
    inOrder.verify(screen).openPatientSummaryScreen(savedPatient.uuid)
  }

  @Test
  fun `when save is clicked with no answers then patient with an empty medical history should be saved and summary screen should be opened`() {
    val savedPatient = PatientMocker.patient(uuid = patientUuid)
    whenever(patientRepository.saveOngoingEntryAsPatient(user, facility)).thenReturn(Single.just(savedPatient))

    uiEvents.onNext(ScreenCreated())
    uiEvents.onNext(SaveMedicalHistoryClicked())

    val inOrder = inOrder(medicalHistoryRepository, patientRepository, screen)
    inOrder.verify(patientRepository).saveOngoingEntryAsPatient(user, facility)
    inOrder.verify(medicalHistoryRepository).save(
        patientUuid = savedPatient.uuid,
        historyEntry = OngoingMedicalHistoryEntry(
            diagnosedWithHypertension = Unanswered,
            isOnTreatmentForHypertension = Unanswered,
            hasHadHeartAttack = Unanswered,
            hasHadStroke = Unanswered,
            hasHadKidneyDisease = Unanswered,
            hasDiabetes = Unanswered))
    inOrder.verify(screen).openPatientSummaryScreen(savedPatient.uuid)
  }

  private fun setupController() {
    val controller = NewMedicalHistoryScreenController(
        medicalHistoryRepository = medicalHistoryRepository,
        patientRepository = patientRepository,
        userSession = userSession,
        facilityRepository = facilityRepository
    )

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }
}
