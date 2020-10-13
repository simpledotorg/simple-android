package org.simple.clinic.summary

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncTag.FREQUENT
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.time.Duration
import java.time.Instant
import java.util.UUID

class PatientSummaryEffectHandlerTest {

  private val uiActions = mock<PatientSummaryUiActions>()
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val missingPhoneReminderRepository = mock<MissingPhoneReminderRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val dataSync = mock<DataSync>()
  private val facilityRepository = mock<FacilityRepository>()
  private val teleconsultFacilityRepository = mock<TeleconsultationFacilityRepository>()

  private val patientSummaryConfig = PatientSummaryConfig(
      bpEditableDuration = Duration.ofMinutes(10),
      numberOfMeasurementsForTeleconsultation = 3
  )

  private val patientUuid = UUID.fromString("67bde563-2cde-4f43-91b4-ba450f0f4d8a")
  private val user = TestData.loggedInUser(uuid = UUID.fromString("39f96341-c043-4059-880e-e32754341a04"))
  private val facility = TestData.facility(uuid = UUID.fromString("94db5d90-d483-4755-892a-97fde5a870fe"))
  private val medicalHistoryUuid = UUID.fromString("78336f4d-071e-47a6-9423-7ab1f57a907e")
  private val uuidGenerator = FakeUuidGenerator.fixed(medicalHistoryUuid)

  private val effectHandler = PatientSummaryEffectHandler(
      schedulersProvider = TrampolineSchedulersProvider(),
      patientRepository = patientRepository,
      bloodPressureRepository = bloodPressureRepository,
      appointmentRepository = mock(),
      missingPhoneReminderRepository = missingPhoneReminderRepository,
      bloodSugarRepository = bloodSugarRepository,
      dataSync = dataSync,
      medicalHistoryRepository = medicalHistoryRepository,
      prescriptionRepository = prescriptionRepository,
      country = TestData.country(),
      patientSummaryConfig = patientSummaryConfig,
      currentUser = Lazy { user },
      currentFacility = Lazy { facility },
      uuidGenerator = uuidGenerator,
      facilityRepository = facilityRepository,
      teleconsultationFacilityRepository = teleconsultFacilityRepository,
      uiActions = uiActions
  )
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the load current user and facility effect is received, the user and current facility must be fetched`() {
    // when
    testCase.dispatch(LoadCurrentUserAndFacility)

    // then
    testCase.assertOutgoingEvents(CurrentUserAndFacilityLoaded(user, facility))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the load patient summary profile is received, then patient summary profile must be fetched`() {
    // given
    val bangladesh = TestData.country(isoCountryCode = "BD")
    val effectHandler = PatientSummaryEffectHandler(
        schedulersProvider = TrampolineSchedulersProvider(),
        patientRepository = patientRepository,
        bloodPressureRepository = bloodPressureRepository,
        appointmentRepository = mock(),
        missingPhoneReminderRepository = missingPhoneReminderRepository,
        bloodSugarRepository = bloodSugarRepository,
        dataSync = dataSync,
        medicalHistoryRepository = medicalHistoryRepository,
        prescriptionRepository = prescriptionRepository,
        country = bangladesh,
        patientSummaryConfig = patientSummaryConfig,
        currentUser = Lazy { user },
        currentFacility = Lazy { facility },
        uuidGenerator = uuidGenerator,
        facilityRepository = facilityRepository,
        teleconsultationFacilityRepository = teleconsultFacilityRepository,
        uiActions = uiActions
    )
    val testCase = EffectHandlerTestCase(effectHandler.build())
    val registeredFacilityUuid = UUID.fromString("1b359ec9-02e2-4f50-bebd-6001f96df57f")
    val patient = TestData.patient(patientUuid, registeredFacilityId = registeredFacilityUuid)
    val patientAddress = TestData.patientAddress(uuid = patient.addressUuid)
    val patientPhoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", BpPassport))
    val bangladeshNationId = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", BangladeshNationalId))
    val facility = TestData.facility(
        uuid = registeredFacilityUuid,
        name = "CHC Obvious"
    )

    val patientProfile = PatientProfile(patient, patientAddress, listOf(patientPhoneNumber), listOf(bangladeshNationId, bpPassport))
    whenever(patientRepository.patientProfile(patientUuid)) doReturn Observable.just<Optional<PatientProfile>>(Just(patientProfile))
    whenever(facilityRepository.facility(registeredFacilityUuid)) doReturn Optional.of(facility)

    // when
    testCase.dispatch(LoadPatientSummaryProfile(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientSummaryProfileLoaded(
        PatientSummaryProfile(
            patient = patient,
            address = patientAddress,
            phoneNumber = patientPhoneNumber,
            bpPassport = bpPassport,
            alternativeId = bangladeshNationId,
            facility = facility
        )
    ))
  }

  @Test
  fun `when the load patient summary profile is received and registered facility is not present, then patient summary profile must be fetched`() {
    // given
    val bangladesh = TestData.country(isoCountryCode = "BD")
    val effectHandler = PatientSummaryEffectHandler(
        schedulersProvider = TrampolineSchedulersProvider(),
        patientRepository = patientRepository,
        bloodPressureRepository = bloodPressureRepository,
        appointmentRepository = mock(),
        missingPhoneReminderRepository = missingPhoneReminderRepository,
        bloodSugarRepository = bloodSugarRepository,
        dataSync = dataSync,
        medicalHistoryRepository = medicalHistoryRepository,
        prescriptionRepository = prescriptionRepository,
        country = bangladesh,
        patientSummaryConfig = patientSummaryConfig,
        currentUser = Lazy { user },
        currentFacility = Lazy { facility },
        uuidGenerator = uuidGenerator,
        facilityRepository = facilityRepository,
        teleconsultationFacilityRepository = teleconsultFacilityRepository,
        uiActions = uiActions
    )
    val testCase = EffectHandlerTestCase(effectHandler.build())
    val patient = TestData.patient(patientUuid)
    val patientAddress = TestData.patientAddress(uuid = patient.addressUuid)
    val patientPhoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", BpPassport))
    val bangladeshNationId = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", BangladeshNationalId))

    val patientProfile = PatientProfile(patient, patientAddress, listOf(patientPhoneNumber), listOf(bangladeshNationId, bpPassport))
    whenever(patientRepository.patientProfile(patientUuid)) doReturn Observable.just<Optional<PatientProfile>>(Just(patientProfile))

    // when
    testCase.dispatch(LoadPatientSummaryProfile(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientSummaryProfileLoaded(
        PatientSummaryProfile(
            patient = patient,
            address = patientAddress,
            phoneNumber = patientPhoneNumber,
            bpPassport = bpPassport,
            alternativeId = bangladeshNationId,
            facility = null
        )
    ))
  }

  @Test
  fun `when edit click effect is received then show edit patient screen`() {
    //given
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid,
        patientAddressUuid = UUID.fromString("d261cde2-b0cb-436e-9612-8b3b7bde0c63")
    )
    val patientSummaryProfile = PatientSummaryProfile(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = null,
        bpPassport = null,
        alternativeId = null,
        facility = facility
    )
    val facility = TestData.facility(uuid = UUID.fromString("94db5d90-d483-4755-892a-97fde5a870fe"))

    //when
    testCase.dispatch(HandleEditClick(patientSummaryProfile, facility))

    //then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showEditPatientScreen(patientSummaryProfile, facility)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the load data for back click effect is received, load the data`() {
    // given
    val screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")

    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("47a70ee3-0d33-4404-9668-59af72390bfd"),
        patientUuid = patientUuid
    )

    whenever(patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp)) doReturn true
    whenever(bloodPressureRepository.bloodPressureCountImmediate(patientUuid)) doReturn 3
    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 2
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    testCase.dispatch(LoadDataForBackClick(patientUuid, screenCreatedTimestamp))

    // then
    testCase.assertOutgoingEvents(DataForBackClickLoaded(
        hasPatientDataChangedSinceScreenCreated = true,
        countOfRecordedMeasurements = 5,
        diagnosisRecorded = medicalHistory.diagnosisRecorded
    ))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the load data for done click effect is received, load the data`() {
    // given
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("47a70ee3-0d33-4404-9668-59af72390bfd"),
        patientUuid = patientUuid
    )

    whenever(bloodPressureRepository.bloodPressureCountImmediate(patientUuid)) doReturn 2
    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 3
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    testCase.dispatch(LoadDataForDoneClick(patientUuid))

    // then
    testCase.assertOutgoingEvents(DataForDoneClickLoaded(
        countOfRecordedMeasurements = 5,
        diagnosisRecorded = medicalHistory.diagnosisRecorded
    ))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the trigger sync effect is received, trigger a sync`() {
    // when
    testCase.dispatch(TriggerSync(BACK_CLICK))

    // then
    verify(dataSync).fireAndForgetSync(FREQUENT)
    verifyNoMoreInteractions(dataSync)
    testCase.assertOutgoingEvents(SyncTriggered(BACK_CLICK))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when show diagnosis error effect is received, then show diagnosis error`() {
    // when
    testCase.dispatch(ShowDiagnosisError)

    // then
    verify(uiActions).showDiagnosisError()
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when the fetch missing phone reminder effect is received, fetch whether reminder has been shown for the patient`() {
    // given
    whenever(missingPhoneReminderRepository.hasShownReminderForPatient(patientUuid)) doReturn true

    // when
    testCase.dispatch(FetchHasShownMissingPhoneReminder(patientUuid))

    // then
    verifyZeroInteractions(uiActions)
    testCase.assertOutgoingEvents(FetchedHasShownMissingPhoneReminder(true))
  }

  @Test
  fun `when the show add phone popup effect is received, show the add phone alert`() {
    // when
    testCase.dispatch(ShowAddPhonePopup(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showAddPhoneDialog(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the mark phone reminder shown effect is received, mark the missing phone reminder as shown for the patient`() {
    // given
    whenever(missingPhoneReminderRepository.markReminderAsShownFor(patientUuid)) doReturn Completable.complete()

    // when
    testCase.dispatch(MarkReminderAsShown(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verifyZeroInteractions(uiActions)
    verify(missingPhoneReminderRepository).markReminderAsShownFor(patientUuid)
  }

  @Test
  fun `when the show contact patient screen effect is received, the contact patient screen must be opened`() {
    // when
    testCase.dispatch(OpenContactPatientScreen(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openPatientContactSheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open teleconsult record screen effect is received, then open the teleconsult record screen`() {
    // given
    val teleconsultRecordId = UUID.fromString("4d317b9a-9db4-4477-b022-360701db0ddb")

    // when
    testCase.dispatch(NavigateToTeleconsultRecordScreen(
        patientUuid = patientUuid,
        teleconsultRecordId = teleconsultRecordId
    ))

    // then
    verify(uiActions).navigateToTeleconsultRecordScreen(patientUuid, teleconsultRecordId)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when load medical officers effect is received, then load medical officers`() {
    // given
    val medicalOfficers = listOf(
        TestData.medicalOfficer(id = UUID.fromString("7e59677b-568d-4381-8d21-931208088262")),
        TestData.medicalOfficer(id = UUID.fromString("3ae9c956-5a96-4734-a4d6-c79f341d09a6"))
    )

    whenever(teleconsultFacilityRepository.medicalOfficersForFacility(facilityId = facility.uuid)) doReturn medicalOfficers

    // when
    testCase.dispatch(LoadMedicalOfficers)

    // then
    testCase.assertOutgoingEvents(MedicalOfficersLoaded(medicalOfficers))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open contact doctor sheet effect is received, then open the contact doctor sheet`() {
    // when
    testCase.dispatch(OpenContactDoctorSheet(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openContactDoctorSheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }
}
