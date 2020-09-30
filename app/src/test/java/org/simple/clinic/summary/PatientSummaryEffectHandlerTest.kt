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
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.summary.teleconsultation.api.TeleconsultInfo
import org.simple.clinic.summary.teleconsultation.api.TeleconsultationApi
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncGroup.FREQUENT
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.net.UnknownHostException
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
  private val teleconsultationApi = mock<TeleconsultationApi>()
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
      teleconsultationApi = teleconsultationApi,
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
        teleconsultationApi = teleconsultationApi,
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
        teleconsultationApi = teleconsultationApi,
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
  fun `when load patient teleconsultation information effect is received, then fetch patient information`() {
    // given
    val bpPassport = TestData.businessId(
        uuid = UUID.fromString("7ebf2c49-8018-41b9-af7c-43afe02483d4"),
        patientUuid = patientUuid,
        identifier = Identifier("1234567", BpPassport),
        metaDataVersion = BusinessId.MetaDataVersion.BpPassportMetaDataV1
    )

    val bloodPressure1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("ad827cfa-1436-4b98-88be-28831543b9f9"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid
    )
    val bloodPressure2 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("95aaf3f5-cafe-4b1f-a91a-14cbae2de12a"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid
    )
    val bloodPressures = listOf(bloodPressure1, bloodPressure2)

    val bloodSugar1 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("81dc9be6-481e-4da0-a975-1998f2850562"),
        patientUuid = patientUuid,
        facilityUuid = facility.uuid
    )

    val bloodSugars = listOf(bloodSugar1)

    val prescription = TestData.prescription(
        uuid = UUID.fromString("38fffa68-bc29-4a67-a6c8-ece61071fe3b"),
        patientUuid = patientUuid
    )
    val prescriptions = listOf(prescription)

    val medicalHistory = TestData.medicalHistory(
        uuid = medicalHistoryUuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = Answer.Yes,
        hasDiabetes = Answer.No
    )

    val patientInformation = PatientTeleconsultationInfo(
        patientUuid = patientUuid,
        teleconsultRecordId = null,
        bpPassport = bpPassport.identifier.displayValue(),
        facility = facility,
        bloodPressures = bloodPressures,
        bloodSugars = bloodSugars,
        prescriptions = prescriptions,
        medicalHistory = medicalHistory,
        nursePhoneNumber = null,
        doctorPhoneNumber = null
    )

    val phoneNumber = TestData.teleconsultPhoneNumber()

    whenever(bloodPressureRepository.newestMeasurementsForPatientImmediate(patientUuid, patientSummaryConfig.numberOfMeasurementsForTeleconsultation)) doReturn bloodPressures
    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn prescriptions
    whenever(bloodSugarRepository.latestMeasurementsImmediate(patientUuid, patientSummaryConfig.numberOfMeasurementsForTeleconsultation)) doReturn bloodSugars
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    testCase.dispatch(LoadPatientTeleconsultationInfo(patientUuid, bpPassport, facility, phoneNumber))

    // then
    testCase.assertOutgoingEvents(PatientTeleconsultationInfoLoaded(patientInformation, phoneNumber))
  }

  @Test
  fun `when contact doctor effect is received, then contact the doctor`() {
    // given
    val teleconsultationPhoneNumber = "+911111111111"
    val patientUuid = UUID.fromString("b00f5efc-8742-420a-b746-d0641f4a65ab")
    val facilityUuid = UUID.fromString("360b1318-9ea5-4dbb-8023-24067722b613")

    val bpPassport = TestData.businessId(
        uuid = UUID.fromString("7ebf2c49-8018-41b9-af7c-43afe02483d4"),
        patientUuid = patientUuid,
        identifier = Identifier("1234567", BpPassport),
        metaDataVersion = BusinessId.MetaDataVersion.BpPassportMetaDataV1
    )
    val facility = TestData.facility(
        uuid = facilityUuid
    )

    val bloodPressure1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("ad827cfa-1436-4b98-88be-28831543b9f9"),
        patientUuid = patientUuid,
        facilityUuid = facilityUuid
    )
    val bloodPressure2 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("95aaf3f5-cafe-4b1f-a91a-14cbae2de12a"),
        patientUuid = patientUuid,
        facilityUuid = facilityUuid
    )
    val bloodPressures = listOf(bloodPressure1, bloodPressure2)

    val bloodSugar1 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("782923da-4f1f-4008-bc17-d2f03871eb54"),
        patientUuid = patientUuid,
        facilityUuid = facilityUuid
    )

    val bloodSugars = listOf(bloodSugar1)

    val prescription1 = TestData.prescription(
        uuid = UUID.fromString("38fffa68-bc29-4a67-a6c8-ece61071fe3b"),
        patientUuid = patientUuid
    )
    val prescriptions = listOf(prescription1)

    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("2fe79564-5d97-479f-99eb-925652bf39d6"),
        patientUuid = patientUuid,
        diagnosedWithHypertension = Answer.Yes,
        hasDiabetes = Answer.No
    )

    val patientInformation = PatientTeleconsultationInfo(
        patientUuid = patientUuid,
        teleconsultRecordId = null,
        bpPassport = bpPassport.identifier.displayValue(),
        facility = facility,
        bloodPressures = bloodPressures,
        bloodSugars = bloodSugars,
        prescriptions = prescriptions,
        medicalHistory = medicalHistory,
        nursePhoneNumber = null,
        doctorPhoneNumber = null
    )

    // when
    testCase.dispatch(ContactDoctor(patientInformation, teleconsultationPhoneNumber))

    // then
    verify(uiActions).contactDoctor(patientInformation, teleconsultationPhoneNumber)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when fetch teleconsultation phone number effect is received, then fetch the phone number`() {
    // given
    val phoneNumber = "+911111111111"
    val phoneNumbers = listOf(TestData.teleconsultPhoneNumber(phoneNumber))

    whenever(teleconsultationApi.get(facility.uuid)) doReturn Single.just(TestData.facilityTeleconsultationsResponse(phoneNumber, phoneNumbers))

    // when
    testCase.dispatch(FetchTeleconsultationInfo(facility.uuid))

    // then
    verifyZeroInteractions(uiActions)
    testCase.assertOutgoingEvents(FetchedTeleconsultationInfo(TeleconsultInfo.Fetched(phoneNumbers)))
  }

  @Test
  fun `when fetched teleconsultation info contains no phone number, then set teleconsult info to missing number`() {
    // given
    whenever(teleconsultationApi.get(facility.uuid)) doReturn Single.just(TestData.facilityTeleconsultationsResponse(null, emptyList()))

    // when
    testCase.dispatch(FetchTeleconsultationInfo(facility.uuid))

    // then
    verifyZeroInteractions(uiActions)
    testCase.assertOutgoingEvents(FetchedTeleconsultationInfo(TeleconsultInfo.MissingPhoneNumber))
  }

  @Test
  fun `when fetching teleconsultation info fails with a network error, then set teleconsult info to network error`() {
    // given
    whenever(teleconsultationApi.get(facility.uuid)) doReturn Single.error(UnknownHostException("Failed to connect to server"))

    // when
    testCase.dispatch(FetchTeleconsultationInfo(facility.uuid))

    // then
    verifyZeroInteractions(uiActions)
    testCase.assertOutgoingEvents(FetchedTeleconsultationInfo(TeleconsultInfo.NetworkError))
  }

  @Test
  fun `when show teleconsult info error is received, then show teleconsult info error snackbar`() {
    // when
    testCase.dispatch(ShowTeleconsultInfoError)

    // then
    verify(uiActions).showTeleconsultInfoError()
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when open contact doctor sheet effect is received, then open the contact sheet`() {
    // given
    val phoneNumbers = listOf(
        TestData.teleconsultPhoneNumber("+911111111111"),
        TestData.teleconsultPhoneNumber("+912222222222")
    )

    // when
    testCase.dispatch(OpenSelectDoctorSheet(facility, phoneNumbers))

    // then
    verify(uiActions).openContactDoctorSheet(facility, phoneNumbers)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
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
}
