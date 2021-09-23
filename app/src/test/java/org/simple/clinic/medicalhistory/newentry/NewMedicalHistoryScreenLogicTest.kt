package org.simple.clinic.medicalhistory.newentry

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_DIABETES_TREATMENT
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_HYPERTENSION_TREATMENT
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class NewMedicalHistoryScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: NewMedicalHistoryUi = mock()
  private val uiActions: NewMedicalHistoryUiActions = mock()
  private val viewRenderer = NewMedicalHistoryUiRenderer(screen)
  private val medicalHistoryRepository: MedicalHistoryRepository = mock()
  private val patientRepository: PatientRepository = mock()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val user = TestData.loggedInUser(uuid = UUID.fromString("4eb3d692-7362-4b10-848a-a7d679aee23a"))
  private val facility = TestData.facility(uuid = UUID.fromString("6fc07446-c508-47e7-998e-8c475f9114d1"))
  private val patientUuid = UUID.fromString("d4f0fb3a-0146-4bc6-afec-95b76c61edca")
  private val medicalHistoryUuid = UUID.fromString("779ae5fb-667b-435e-888c-8386397ed1f1")

  private val dateOfBirthFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private lateinit var testFixture: MobiusTestFixture<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when screen is started then the patient's name should be shown on the toolbar`() {
    val patientName = "Ashok Kumar"
    val patientEntry = OngoingNewPatientEntry(personalDetails = PersonalDetails(
        fullName = patientName,
        dateOfBirth = null,
        age = "20",
        gender = Gender.Transgender))
    whenever(patientRepository.ongoingEntry()).thenReturn(patientEntry)

    startMobiusLoop(ongoingPatientEntry = patientEntry)

    // This gets set twice:
    // 1. When we read the patient entry
    // 2. When we load the current facility and update the model
    verify(screen, times(2)).setPatientName(patientName)
  }

  @Test
  fun `when save is clicked with selected answers then patient with the answers should be saved and summary screen should be opened`() {
    // given
    val addressUuid = UUID.fromString("79f6d1bc-0e10-480b-9cce-43c628d827b7")
    val savedPatient = TestData.patientProfile(
        patientUuid = patientUuid,
        patientAddressUuid = addressUuid
    )
    val patientEntry = savedPatient.toOngoingEntry()

    val uuidGenerator = mock<UuidGenerator>()
    whenever(uuidGenerator.v4()).thenReturn(patientUuid, addressUuid, medicalHistoryUuid)

    whenever(patientRepository.saveOngoingEntryAsPatient(
        patientEntry = eq(patientEntry),
        loggedInUser = eq(user),
        facility = eq(facility),
        patientUuid = eq(patientUuid),
        addressUuid = eq(addressUuid),
        supplyUuidForBpPassport = any(),
        supplyUuidForAlternativeId = any(),
        supplyUuidForPhoneNumber = any(),
        dateOfBirthFormatter = eq(dateOfBirthFormatter)
    )).thenReturn(savedPatient)

    // when
    startMobiusLoop(uuidGenerator = uuidGenerator, ongoingPatientEntry = patientEntry)

    uiEvents.onNext(NewMedicalHistoryAnswerToggled(DIAGNOSED_WITH_HYPERTENSION, No))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(HAS_HAD_A_HEART_ATTACK, No))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(HAS_HAD_A_STROKE, No))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(HAS_HAD_A_KIDNEY_DISEASE, Yes))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(DIAGNOSED_WITH_DIABETES, Yes))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(IS_ON_DIABETES_TREATMENT, Yes))
    uiEvents.onNext(SaveMedicalHistoryClicked())

    // then
    with(inOrder(medicalHistoryRepository, patientRepository, uiActions)) {
      verify(patientRepository).saveOngoingEntryAsPatient(
          patientEntry = eq(patientEntry),
          loggedInUser = eq(user),
          facility = eq(facility),
          patientUuid = eq(patientUuid),
          addressUuid = eq(addressUuid),
          supplyUuidForBpPassport = any(),
          supplyUuidForAlternativeId = any(),
          supplyUuidForPhoneNumber = any(),
          dateOfBirthFormatter = eq(dateOfBirthFormatter)
      )
      verify(medicalHistoryRepository).save(
          uuid = medicalHistoryUuid,
          patientUuid = savedPatient.patientUuid,
          historyEntry = OngoingMedicalHistoryEntry(
              hasHadHeartAttack = No,
              hasHadStroke = No,
              hasHadKidneyDisease = Yes,
              diagnosedWithHypertension = No,
              hasDiabetes = Yes,
              isOnDiabetesTreatment = Yes
          )
      )
      verify(uiActions).openPatientSummaryScreen(savedPatient.patientUuid)
    }
  }

  @Test
  fun `when save is clicked with no answers then patient with an empty medical history should be saved and summary screen should be opened`() {
    // given
    val addressUuid = UUID.fromString("79f6d1bc-0e10-480b-9cce-43c628d827b7")
    val savedPatient = TestData.patientProfile(
        patientUuid = patientUuid,
        patientAddressUuid = addressUuid
    )
    val patientEntry = savedPatient.toOngoingEntry()

    val uuidGenerator = mock<UuidGenerator>()
    whenever(uuidGenerator.v4()).thenReturn(patientUuid, addressUuid, medicalHistoryUuid)

    whenever(patientRepository.saveOngoingEntryAsPatient(
        patientEntry = eq(patientEntry),
        loggedInUser = eq(user),
        facility = eq(facility),
        patientUuid = eq(patientUuid),
        addressUuid = eq(addressUuid),
        supplyUuidForBpPassport = any(),
        supplyUuidForAlternativeId = any(),
        supplyUuidForPhoneNumber = any(),
        dateOfBirthFormatter = eq(dateOfBirthFormatter)
    )).thenReturn(savedPatient)

    // when
    startMobiusLoop(uuidGenerator = uuidGenerator, ongoingPatientEntry = patientEntry)

    uiEvents.onNext(NewMedicalHistoryAnswerToggled(DIAGNOSED_WITH_HYPERTENSION, Yes))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(IS_ON_HYPERTENSION_TREATMENT, Yes))
    uiEvents.onNext(SaveMedicalHistoryClicked())

    // then
    with(inOrder(medicalHistoryRepository, patientRepository, uiActions)) {
      verify(patientRepository).saveOngoingEntryAsPatient(
          patientEntry = eq(patientEntry),
          loggedInUser = eq(user),
          facility = eq(facility),
          patientUuid = eq(patientUuid),
          addressUuid = eq(addressUuid),
          supplyUuidForBpPassport = any(),
          supplyUuidForAlternativeId = any(),
          supplyUuidForPhoneNumber = any(),
          dateOfBirthFormatter = eq(dateOfBirthFormatter)
      )
      verify(medicalHistoryRepository).save(
          uuid = medicalHistoryUuid,
          patientUuid = savedPatient.patientUuid,
          historyEntry = OngoingMedicalHistoryEntry(
              // We currently default the hypertension diagnosis answer to 'Yes' if the facility
              // does not support diabetes management. The mock facility we use in tests has DM
              // off by default, so this is hidden behaviour.
              hasHadHeartAttack = Unanswered,
              hasHadStroke = Unanswered,
              hasHadKidneyDisease = Unanswered,
              diagnosedWithHypertension = Yes,
              hasDiabetes = Unanswered,
              isOnHypertensionTreatment = Yes))
      verify(uiActions).openPatientSummaryScreen(savedPatient.patientUuid)
    }
  }

  @Test
  fun `when an already selected answer for a question is changed, the new answer should be used when saving the medical history`() {
    // given
    val addressUuid = UUID.fromString("79f6d1bc-0e10-480b-9cce-43c628d827b7")
    val savedPatient = TestData.patientProfile(
        patientUuid = patientUuid,
        patientAddressUuid = addressUuid
    )
    val patientEntry = savedPatient.toOngoingEntry()

    val uuidGenerator = mock<UuidGenerator>()
    whenever(uuidGenerator.v4()).thenReturn(patientUuid, addressUuid, medicalHistoryUuid)

    whenever(patientRepository.saveOngoingEntryAsPatient(
        patientEntry = eq(patientEntry),
        loggedInUser = eq(user),
        facility = eq(facility),
        patientUuid = eq(patientUuid),
        addressUuid = eq(addressUuid),
        supplyUuidForBpPassport = any(),
        supplyUuidForAlternativeId = any(),
        supplyUuidForPhoneNumber = any(),
        dateOfBirthFormatter = eq(dateOfBirthFormatter)
    )).thenReturn(savedPatient)

    // when
    startMobiusLoop(uuidGenerator = uuidGenerator, ongoingPatientEntry = patientEntry)

    // Initial answers
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(DIAGNOSED_WITH_HYPERTENSION, No))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(HAS_HAD_A_HEART_ATTACK, No))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(HAS_HAD_A_STROKE, No))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(HAS_HAD_A_KIDNEY_DISEASE, Yes))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(DIAGNOSED_WITH_DIABETES, Yes))

    // Updated answers
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(DIAGNOSED_WITH_HYPERTENSION, Yes))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(HAS_HAD_A_HEART_ATTACK, Unanswered))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(HAS_HAD_A_STROKE, Unanswered))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(HAS_HAD_A_KIDNEY_DISEASE, No))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(DIAGNOSED_WITH_DIABETES, No))
    uiEvents.onNext(NewMedicalHistoryAnswerToggled(IS_ON_HYPERTENSION_TREATMENT, Yes))

    uiEvents.onNext(SaveMedicalHistoryClicked())

    // then
    with(inOrder(medicalHistoryRepository, patientRepository, uiActions)) {
      verify(patientRepository).saveOngoingEntryAsPatient(
          patientEntry = eq(patientEntry),
          loggedInUser = eq(user),
          facility = eq(facility),
          patientUuid = eq(patientUuid),
          addressUuid = eq(addressUuid),
          supplyUuidForBpPassport = any(),
          supplyUuidForAlternativeId = any(),
          supplyUuidForPhoneNumber = any(),
          dateOfBirthFormatter = eq(dateOfBirthFormatter)
      )
      verify(medicalHistoryRepository).save(
          uuid = medicalHistoryUuid,
          patientUuid = savedPatient.patientUuid,
          historyEntry = OngoingMedicalHistoryEntry(
              hasHadHeartAttack = Unanswered,
              hasHadStroke = Unanswered,
              hasHadKidneyDisease = No,
              diagnosedWithHypertension = Yes,
              hasDiabetes = No,
              isOnHypertensionTreatment = Yes
          )
      )
      verify(uiActions).openPatientSummaryScreen(savedPatient.patientUuid)
    }
  }

  private fun startMobiusLoop(
      ongoingPatientEntry: OngoingNewPatientEntry = OngoingNewPatientEntry.fromFullName("Anish Acharya"),
      uuidGenerator: UuidGenerator = FakeUuidGenerator.fixed(medicalHistoryUuid)
  ) {
    whenever(medicalHistoryRepository.save(eq(medicalHistoryUuid), eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.ongoingEntry()).thenReturn(ongoingPatientEntry)

    val viewEffectHandler = NewMedicalHistoryViewEffectHandler(uiActions)

    val effectHandler = NewMedicalHistoryEffectHandler(
        schedulersProvider = TrampolineSchedulersProvider(),
        patientRepository = patientRepository,
        medicalHistoryRepository = medicalHistoryRepository,
        dataSync = mock(),
        currentUser = Lazy { user },
        currentFacility = Lazy { facility },
        uuidGenerator = uuidGenerator,
        dateOfBirthFormatter = dateOfBirthFormatter,
        viewEffectsConsumer = viewEffectHandler::handle
    ).build()

    val country = TestData.country(isoCountryCode = Country.INDIA)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = NewMedicalHistoryModel.default(country),
        init = NewMedicalHistoryInit(),
        update = NewMedicalHistoryUpdate(),
        effectHandler = effectHandler,
        modelUpdateListener = viewRenderer::render
    )

    testFixture.start()
  }
}

private fun PatientProfile.toOngoingEntry(): OngoingNewPatientEntry {
  return OngoingNewPatientEntry.fromFullName(patient.fullName)
}
