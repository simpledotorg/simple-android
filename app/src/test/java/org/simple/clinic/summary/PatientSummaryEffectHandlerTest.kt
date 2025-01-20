package org.simple.clinic.summary

import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.cvdrisk.CVDRiskCalculator
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.CVDRiskRepository
import org.simple.clinic.cvdrisk.StatinInfo
import org.simple.clinic.drugs.DiagnosisWarningPrescriptions
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.Appointment.Status
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.Answer
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patientattribute.BMIReading
import org.simple.clinic.patientattribute.PatientAttributeRepository
import org.simple.clinic.reassignpatient.ReassignPatientSheetOpenedFrom
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.BACK_CLICK
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.TestData
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.uuid.FakeUuidGenerator
import java.time.Instant
import java.util.Optional
import java.util.UUID

class PatientSummaryEffectHandlerTest {

  private val uiActions = mock<PatientSummaryUiActions>()
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val missingPhoneReminderRepository = mock<MissingPhoneReminderRepository>()
  private val cvdRiskRepository = mock<CVDRiskRepository>()
  private val patientAttributeRepository = mock<PatientAttributeRepository>()
  private val dataSync = mock<DataSync>()
  private val facilityRepository = mock<FacilityRepository>()
  private val teleconsultFacilityRepository = mock<TeleconsultationFacilityRepository>()
  private val appointmentRepository = mock<AppointmentRepository>()
  private val viewEffectHandler = PatientSummaryViewEffectHandler(uiActions)

  private val patientUuid = UUID.fromString("67bde563-2cde-4f43-91b4-ba450f0f4d8a")
  private val user = TestData.loggedInUser(uuid = UUID.fromString("39f96341-c043-4059-880e-e32754341a04"))
  private val facility = TestData.facility(uuid = UUID.fromString("94db5d90-d483-4755-892a-97fde5a870fe"))
  private val medicalHistoryUuid = UUID.fromString("78336f4d-071e-47a6-9423-7ab1f57a907e")
  private val uuidGenerator = FakeUuidGenerator.fixed(medicalHistoryUuid)

  private val clock = TestUtcClock()
  private val userClock = TestUserClock(instant = Instant.parse("2018-01-01T00:00:00Z"))
  private val diagnosisWarningPrescriptions = DiagnosisWarningPrescriptions(
      htnPrescriptions = listOf("amlodipine"),
      diabetesPrescriptions = listOf("metformin")
  )
  private val cvdRiskCalculator = CVDRiskCalculator({ TestData.cvdRiskCalculationSheet() })

  private val effectHandler = PatientSummaryEffectHandler(
      clock = clock,
      userClock = userClock,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      bloodPressureRepository = bloodPressureRepository,
      appointmentRepository = appointmentRepository,
      missingPhoneReminderRepository = missingPhoneReminderRepository,
      bloodSugarRepository = bloodSugarRepository,
      dataSync = dataSync,
      medicalHistoryRepository = medicalHistoryRepository,
      country = TestData.country(),
      currentUser = { user },
      currentFacility = { facility },
      uuidGenerator = uuidGenerator,
      facilityRepository = facilityRepository,
      teleconsultationFacilityRepository = teleconsultFacilityRepository,
      prescriptionRepository = prescriptionRepository,
      cdssPilotFacilities = { emptyList() },
      diagnosisWarningPrescriptions = { diagnosisWarningPrescriptions },
      cvdRiskRepository = cvdRiskRepository,
      viewEffectsConsumer = viewEffectHandler::handle,
      cvdRiskCalculator = cvdRiskCalculator,
      patientAttributeRepository = patientAttributeRepository,
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
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the load patient summary profile is received, then patient summary profile must be fetched`() {
    // given
    val bangladesh = TestData.country(isoCountryCode = "BD")
    val effectHandler = PatientSummaryEffectHandler(
        clock = clock,
        userClock = userClock,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        patientRepository = patientRepository,
        bloodPressureRepository = bloodPressureRepository,
        appointmentRepository = mock(),
        missingPhoneReminderRepository = missingPhoneReminderRepository,
        bloodSugarRepository = bloodSugarRepository,
        dataSync = dataSync,
        medicalHistoryRepository = medicalHistoryRepository,
        country = bangladesh,
        currentUser = { user },
        currentFacility = { facility },
        uuidGenerator = uuidGenerator,
        facilityRepository = facilityRepository,
        teleconsultationFacilityRepository = teleconsultFacilityRepository,
        prescriptionRepository = prescriptionRepository,
        cdssPilotFacilities = { emptyList() },
        viewEffectsConsumer = viewEffectHandler::handle,
        diagnosisWarningPrescriptions = { diagnosisWarningPrescriptions },
        cvdRiskRepository = cvdRiskRepository,
        cvdRiskCalculator = cvdRiskCalculator,
        patientAttributeRepository = patientAttributeRepository,
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
    whenever(patientRepository.patientProfile(patientUuid)) doReturn Observable.just<Optional<PatientProfile>>(Optional.of(patientProfile))
    whenever(facilityRepository.facility(registeredFacilityUuid)) doReturn Optional.of(facility)

    // when
    testCase.dispatch(LoadPatientSummaryProfile(patientUuid))

    // then
    testCase.assertOutgoingEvents(
        PatientSummaryProfileLoaded(
            PatientSummaryProfile(
                patient = patient,
                address = patientAddress,
                phoneNumber = patientPhoneNumber,
                bpPassport = bpPassport,
                alternativeId = bangladeshNationId,
                facility = facility
            )
        )
    )
  }

  @Test
  fun `when the load patient summary profile is received and registered facility is not present, then patient summary profile must be fetched`() {
    // given
    val bangladesh = TestData.country(isoCountryCode = "BD")
    val effectHandler = PatientSummaryEffectHandler(
        clock = clock,
        userClock = TestUserClock(),
        schedulersProvider = TestSchedulersProvider.trampoline(),
        patientRepository = patientRepository,
        bloodPressureRepository = bloodPressureRepository,
        appointmentRepository = mock(),
        missingPhoneReminderRepository = missingPhoneReminderRepository,
        bloodSugarRepository = bloodSugarRepository,
        dataSync = dataSync,
        medicalHistoryRepository = medicalHistoryRepository,
        country = bangladesh,
        currentUser = { user },
        currentFacility = { facility },
        uuidGenerator = uuidGenerator,
        facilityRepository = facilityRepository,
        teleconsultationFacilityRepository = teleconsultFacilityRepository,
        prescriptionRepository = prescriptionRepository,
        cdssPilotFacilities = { emptyList() },
        diagnosisWarningPrescriptions = { diagnosisWarningPrescriptions },
        viewEffectsConsumer = viewEffectHandler::handle,
        cvdRiskRepository = cvdRiskRepository,
        cvdRiskCalculator = cvdRiskCalculator,
        patientAttributeRepository = patientAttributeRepository,
    )
    val testCase = EffectHandlerTestCase(effectHandler.build())
    val patient = TestData.patient(patientUuid)
    val patientAddress = TestData.patientAddress(uuid = patient.addressUuid)
    val patientPhoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", BpPassport))
    val bangladeshNationId = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", BangladeshNationalId))

    val patientProfile = PatientProfile(patient, patientAddress, listOf(patientPhoneNumber), listOf(bangladeshNationId, bpPassport))
    whenever(patientRepository.patientProfile(patientUuid)) doReturn Observable.just<Optional<PatientProfile>>(Optional.of(patientProfile))

    // when
    testCase.dispatch(LoadPatientSummaryProfile(patientUuid))

    // then
    testCase.assertOutgoingEvents(
        PatientSummaryProfileLoaded(
            PatientSummaryProfile(
                patient = patient,
                address = patientAddress,
                phoneNumber = patientPhoneNumber,
                bpPassport = bpPassport,
                alternativeId = bangladeshNationId,
                facility = null
            )
        )
    )
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
    val prescribedDrugs = listOf(
        TestData.prescription(
            uuid = UUID.fromString("48e8750b-0aca-4da9-81e0-4fb24b3b18f8"),
            name = "Amlodipine"
        )
    )

    whenever(patientRepository.hasPatientMeasurementDataChangedSince(patientUuid, screenCreatedTimestamp)) doReturn true
    whenever(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, screenCreatedTimestamp)) doReturn false
    whenever(bloodPressureRepository.bloodPressureCountImmediate(patientUuid)) doReturn 3
    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 2
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory
    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn prescribedDrugs

    // when
    testCase.dispatch(LoadDataForBackClick(patientUuid, screenCreatedTimestamp, false))

    // then
    testCase.assertOutgoingEvents(
        DataForBackClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 3,
            countOfRecordedBloodSugars = 2,
            medicalHistory = medicalHistory,
            canShowPatientReassignmentWarning = false,
            prescribedDrugs = prescribedDrugs,
            diagnosisWarningPrescriptions = diagnosisWarningPrescriptions
        )
    )
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the load data for done click effect is received, load the data`() {
    // given
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("47a70ee3-0d33-4404-9668-59af72390bfd"),
        patientUuid = patientUuid,
        diagnosedWithHypertension = Yes,
        hasDiabetes = No
    )
    val prescribedDrugs = listOf(
        TestData.prescription(
            uuid = UUID.fromString("48e8750b-0aca-4da9-81e0-4fb24b3b18f8"),
            name = "Amlodipine"
        )
    )

    whenever(patientRepository.hasPatientMeasurementDataChangedSince(patientUuid, Instant.parse("2018-01-01T00:00:00Z"))) doReturn true
    whenever(appointmentRepository.hasAppointmentForPatientChangedSince(patientUuid, Instant.parse("2018-01-01T00:00:00Z"))) doReturn false
    whenever(bloodPressureRepository.bloodPressureCountImmediate(patientUuid)) doReturn 2
    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 3
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory
    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn prescribedDrugs

    // when
    testCase.dispatch(LoadDataForDoneClick(
        patientUuid = patientUuid,
        screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
        canShowPatientReassignmentWarning = true
    ))

    // then
    testCase.assertOutgoingEvents(
        DataForDoneClickLoaded(
            hasPatientMeasurementDataChangedSinceScreenCreated = true,
            hasAppointmentChangeSinceScreenCreated = false,
            countOfRecordedBloodPressures = 2,
            countOfRecordedBloodSugars = 3,
            medicalHistory = medicalHistory,
            canShowPatientReassignmentWarning = true,
            prescribedDrugs = prescribedDrugs,
            diagnosisWarningPrescriptions = diagnosisWarningPrescriptions
        )
    )
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the trigger sync effect is received, trigger a sync`() {
    // when
    testCase.dispatch(TriggerSync(BACK_CLICK))

    // then
    verify(dataSync).fireAndForgetSync()
    verifyNoMoreInteractions(dataSync)
    testCase.assertOutgoingEvents(SyncTriggered(BACK_CLICK))
    verifyNoInteractions(uiActions)
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
    verifyNoInteractions(uiActions)
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
    verifyNoInteractions(uiActions)
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
    testCase.dispatch(
        NavigateToTeleconsultRecordScreen(
            patientUuid = patientUuid,
            teleconsultRecordId = teleconsultRecordId
        )
    )

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
    verifyNoInteractions(uiActions)
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

  @Test
  fun `when show add measurements warning dialog effect is received, then show add measurements warning dialog`() {
    // when
    testCase.dispatch(ShowAddMeasurementsWarningDialog)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showAddMeasurementsWarningDialog()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show add blood pressure warning dialog effect is received, then show add blood pressure warning dialog`() {
    // when
    testCase.dispatch(ShowAddBloodPressureWarningDialog)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showAddBloodPressureWarningDialog()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show add blood sugar warning dialog effect is received, then show add blood sugar warning dialog`() {
    // when
    testCase.dispatch(ShowAddBloodSugarWarningDialog)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showAddBloodSugarWarningDialog()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open select facility sheet effect is received, then show the select facility sheet`() {
    // when
    testCase.dispatch(OpenSelectFacilitySheet)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openSelectFacilitySheet()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when dispatch new assigned facility is received, then dispatch the newly selected facility`() {
    // given
    val selectedFacility = TestData.facility(uuid = UUID.fromString("68a067f0-e746-4191-bfe4-3c4a026642b8"))

    // when
    testCase.dispatch(DispatchNewAssignedFacility(selectedFacility))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).dispatchNewAssignedFacility(selectedFacility)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show update phone popup view effect is received, then show update phone popup`() {
    // given
    val patientUuid = UUID.fromString("be201fe0-f65c-463d-a302-8b67ff2c4c55")

    // when
    testCase.dispatch(ShowUpdatePhonePopup(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showUpdatePhoneDialog(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load patient registration data effect is received, then load patient registration data`() {
    // given
    val patientUuid = UUID.fromString("31b34900-52aa-4892-8bed-2c720951880e")
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("333a13f9-4a5b-4fd9-84f3-e169d26331ba"),
        patientUuid = patientUuid
    )

    whenever(prescriptionRepository.prescriptionCountImmediate(patientUuid)) doReturn 2
    whenever(bloodPressureRepository.bloodPressureCountImmediate(patientUuid)) doReturn 2
    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 0

    // when
    testCase.dispatch(LoadPatientRegistrationData(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientRegistrationDataLoaded(
        countOfPrescribedDrugs = 2,
        countOfRecordedBloodPressures = 2,
        countOfRecordedBloodSugars = 0
    ))

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when refresh next appointment effect is received, then refresh appointment details`() {
    // when
    testCase.dispatch(RefreshNextAppointment)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).refreshNextAppointment()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load clinical decision support effect is received, then load clinical decision support info`() {
    // given
    val patientUuid = UUID.fromString("44daeb85-de4c-4807-8b31-6a88bf597cc7")

    whenever(bloodPressureRepository.isNewestBpEntryHigh(patientUuid)).doReturn(Observable.just(true))
    whenever(prescriptionRepository.hasPrescriptionForPatientChangedToday(patientUuid)).doReturn(Observable.just(true))

    // when
    testCase.dispatch(LoadClinicalDecisionSupportInfo(patientUuid))

    // then
    testCase.assertOutgoingEvents(
        ClinicalDecisionSupportInfoLoaded(
            isNewestBpEntryHigh = true,
            hasPrescribedDrugsChangedToday = true
        ))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when check if cdss pilot is enabled effect is received, then check the cdss enabled status`() {
    // given
    val cdssPilotFacilities = listOf(UUID.fromString("635bf319-d6bb-426e-b5f0-6003614ad4fe"))
    val facility = TestData.facility(
        uuid = UUID.fromString("635bf319-d6bb-426e-b5f0-6003614ad4fe"),
        name = "CHC Obvious"
    )
    val effectHandler = PatientSummaryEffectHandler(
        clock = clock,
        userClock = userClock,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        patientRepository = patientRepository,
        bloodPressureRepository = bloodPressureRepository,
        appointmentRepository = appointmentRepository,
        missingPhoneReminderRepository = missingPhoneReminderRepository,
        bloodSugarRepository = bloodSugarRepository,
        dataSync = dataSync,
        medicalHistoryRepository = medicalHistoryRepository,
        country = TestData.country(),
        currentUser = { user },
        currentFacility = { facility },
        uuidGenerator = uuidGenerator,
        facilityRepository = facilityRepository,
        teleconsultationFacilityRepository = teleconsultFacilityRepository,
        prescriptionRepository = prescriptionRepository,
        cdssPilotFacilities = { cdssPilotFacilities },
        diagnosisWarningPrescriptions = { diagnosisWarningPrescriptions },
        cvdRiskRepository = cvdRiskRepository,
        viewEffectsConsumer = viewEffectHandler::handle,
        cvdRiskCalculator = cvdRiskCalculator,
        patientAttributeRepository = patientAttributeRepository,
    )
    val testCase = EffectHandlerTestCase(effectHandler = effectHandler.build())

    // when
    testCase.dispatch(CheckIfCDSSPilotIsEnabled)

    // then
    verifyNoInteractions(uiActions)

    testCase.assertOutgoingEvents(CDSSPilotStatusChecked(isPilotEnabledForFacility = true))
    testCase.dispose()
  }

  @Test
  fun `when load latest scheduled appointment effect is received, then load the latest scheduled appointment`() {
    // given
    val patientUuid = UUID.fromString("952ad986-3b43-4567-bd86-79937b42759a")
    val appointment = TestData.appointment(
        uuid = UUID.fromString("5e27be58-ed65-4949-9dbb-fd7df12f9a1d"),
        patientUuid = patientUuid,
        status = Status.Scheduled
    )

    whenever(appointmentRepository.latestScheduledAppointmentForPatient(patientUuid)) doReturn appointment

    // when
    testCase.dispatch(LoadLatestScheduledAppointment(patientUuid))

    // then
    testCase.assertOutgoingEvents(LatestScheduledAppointmentLoaded(appointment))

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when show reassign patient sheet effect is received, then show the sheet`() {
    // given
    val patientUuid = UUID.fromString("1234d26f-fa70-44de-a4ee-721378d9fa07")
    val facility = TestData.facility(
        uuid = UUID.fromString("a8539a86-ba91-427f-b5d6-27dc058fbd4a"),
        name = "PHC Simple"
    )

    // when
    testCase.dispatch(ShowReassignPatientWarningSheet(
        patientUuid = patientUuid,
        currentFacility = facility,
        sheetOpenedFrom = ReassignPatientSheetOpenedFrom.DONE_CLICK
    ))

    // then
    verify(uiActions).showReassignPatientWarningSheet(patientUuid, facility, ReassignPatientSheetOpenedFrom.DONE_CLICK)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when update patient reassignment status effect is received, then update the status`() {
    // given
    val patientUuid = UUID.fromString("938efb41-8117-43f2-ae1a-410d64a0e204")

    // when
    testCase.dispatch(UpdatePatientReassignmentStatus(patientUuid = patientUuid, status = Answer.Yes))

    // then
    verify(patientRepository).updatePatientReassignmentEligibilityStatus(patientUuid, Answer.Yes)
    verifyNoMoreInteractions(patientRepository)

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when check patient reassignment status effect is received, then check patient reassignment status`() {
    // given
    val patientUuid = UUID.fromString("eb787ae3-4757-4f3c-a4c1-949d489971a5")

    whenever(patientRepository.isPatientEligibleForReassignment(patientUuid)) doReturn true

    // when
    testCase.dispatch(CheckPatientReassignmentStatus(
        patientUuid = patientUuid,
        clickAction = ClickAction.DONE,
        screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
    ))

    // then
    testCase.assertOutgoingEvents(PatientReassignmentStatusLoaded(
        isPatientEligibleForReassignment = true,
        clickAction = ClickAction.DONE,
        screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
    ))
  }

  @Test
  fun `when mark diabetes diagnosis effect is received, then update the diabetes diagnosis status`() {
    // given
    val patientUuid = UUID.fromString("522a2e9f-46d2-4181-bfcc-8a9891cd49b5")
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("17e50ff6-6ba9-4d7a-b3c9-8966114fac1b"),
        hasDiabetes = No
    )

    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        patientUuid = patientUuid,
        defaultHistoryUuid = uuidGenerator.v4()
    )) doReturn medicalHistory

    // when
    testCase.dispatch(MarkDiabetesDiagnosis(patientUuid))

    // then
    val updatedMedicalHistory = medicalHistory.copy(diagnosedWithDiabetes = Yes)
    verify(medicalHistoryRepository).save(updatedMedicalHistory, Instant.now(clock))
  }

  @Test
  fun `when show diabetes diagnosis warning view effect is received, then show the warning dialog`() {
    // when
    testCase.dispatch(ShowDiabetesDiagnosisWarning)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showDiabetesDiagnosisWarning()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when mark htn diagnosis effect is received, then update the htn diagnosis status`() {
    // given
    val patientUuid = UUID.fromString("f17655ec-3f67-4946-895a-fce42b5de71d")
    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("f921c665-0f96-436b-bfdc-f3562abe43a5"),
        diagnosedWithHypertension = No
    )

    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        patientUuid = patientUuid,
        defaultHistoryUuid = uuidGenerator.v4()
    )) doReturn medicalHistory

    // when
    testCase.dispatch(MarkHypertensionDiagnosis(patientUuid))

    // then
    val updatedMedicalHistory = medicalHistory.copy(diagnosedWithHypertension = Yes)
    verify(medicalHistoryRepository).save(updatedMedicalHistory, Instant.now(clock))
  }

  @Test
  fun `when show htn diagnosis warning view effect is received, then show the warning dialog`() {
    // when
    testCase.dispatch(ShowHypertensionDiagnosisWarning(continueToDiabetesDiagnosisWarning = false))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showHypertensionDiagnosisWarning(continueToDiabetesDiagnosisWarning = false)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load statin prescription check info effect is received, then load info`() {
    // given
    val bangladesh = TestData.country(isoCountryCode = "BD")
    val effectHandler = PatientSummaryEffectHandler(
        clock = clock,
        userClock = userClock,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        patientRepository = patientRepository,
        bloodPressureRepository = bloodPressureRepository,
        appointmentRepository = mock(),
        missingPhoneReminderRepository = missingPhoneReminderRepository,
        bloodSugarRepository = bloodSugarRepository,
        dataSync = dataSync,
        medicalHistoryRepository = medicalHistoryRepository,
        country = bangladesh,
        currentUser = { user },
        currentFacility = { facility },
        uuidGenerator = uuidGenerator,
        facilityRepository = facilityRepository,
        teleconsultationFacilityRepository = teleconsultFacilityRepository,
        prescriptionRepository = prescriptionRepository,
        cdssPilotFacilities = { emptyList() },
        viewEffectsConsumer = viewEffectHandler::handle,
        diagnosisWarningPrescriptions = { diagnosisWarningPrescriptions },
        cvdRiskRepository = cvdRiskRepository,
        cvdRiskCalculator = cvdRiskCalculator,
        patientAttributeRepository = patientAttributeRepository,
    )
    val testCase = EffectHandlerTestCase(effectHandler.build())
    val assignedFacilityId = UUID.fromString("079784fd-de89-4499-9371-f8ae64f26f70")
    val patient = TestData.patient(
        uuid = patientUuid,
        status = PatientStatus.Active,
        assignedFacilityId = assignedFacilityId,
        patientAgeDetails = PatientAgeDetails(
            ageValue = 50,
            ageUpdatedAt = Instant.parse("2018-01-01T00:00:00Z"),
            dateOfBirth = null,
        )
    )

    val medicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("281f2524-8864-4b0f-859c-97952d881ccb"),
        diagnosedWithHypertension = No
    )

    whenever(bloodPressureRepository.hasBPRecordedToday(
        patientUuid = patientUuid,
        today = Instant.parse("2018-01-01T00:00:00Z"),
    )) doReturn Observable.just(true)
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        patientUuid = patientUuid,
        defaultHistoryUuid = uuidGenerator.v4()
    )) doReturn medicalHistory
    whenever(prescriptionRepository.newestPrescriptionsForPatient(patientUuid)) doReturn Observable.just(emptyList())

    // when
    testCase.dispatch(LoadStatinPrescriptionCheckInfo(patient))

    // then
    testCase.assertOutgoingEvents(StatinPrescriptionCheckInfoLoaded(
        age = 50,
        isPatientDead = false,
        hasBPRecordedToday = true,
        medicalHistory = medicalHistory,
        prescriptions = emptyList(),
    ))

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when load cvd risk effect is received, then load cvd risk`() {
    //given
    val cvdRisk = TestData.cvdRisk(riskScore = CVDRiskRange(27, 27))
    whenever(cvdRiskRepository.getCVDRiskImmediate(patientUuid)) doReturn cvdRisk
    whenever(medicalHistoryRepository.hasMedicalHistoryForPatientChangedSince(
        patientUuid = patientUuid,
        instant = cvdRisk.timestamps.updatedAt
    )) doReturn Observable.just(false)

    //when
    testCase.dispatch(LoadCVDRisk(patientUuid))

    //then
    testCase.assertOutgoingEvents(CVDRiskLoaded(cvdRisk.riskScore, false))
  }

  @Test
  fun `when calculate cvd risk effect is received, then calculate cvd risk`() {
    //given
    val patient = TestData.patient(
        uuid = patientUuid,
        gender = Gender.Male,
        status = PatientStatus.Active,
        patientAgeDetails = PatientAgeDetails(
            ageValue = 40,
            ageUpdatedAt = Instant.parse("2018-01-01T00:00:00Z"),
            dateOfBirth = null,
        )
    )
    val medicalHistory = TestData.medicalHistory(isSmoking = Yes)
    val bloodPressure = TestData.bloodPressureMeasurement(
        UUID.fromString("3e8c246f-91b9-4f8c-81fe-91b67ac0a2d5"),
        systolic = 130,
        patientUuid = patientUuid
    )
    val bloodPressures = listOf(bloodPressure)


    whenever(bloodPressureRepository.newestMeasurementsForPatientImmediate(patientUuid = patientUuid, limit = 1)) doReturn bloodPressures
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        patientUuid = patientUuid,
        defaultHistoryUuid = uuidGenerator.v4()
    )) doReturn medicalHistory
    whenever(patientAttributeRepository.getPatientAttributeImmediate(
        patientUuid = patientUuid,
    )) doReturn null

    //when
    testCase.dispatch(CalculateCVDRisk(patient = patient))

    //then
    testCase.assertOutgoingEvents(CVDRiskCalculated(CVDRiskRange(6, 8)))
  }

  @Test
  fun `when load statin info effect is received, then load statin info`() {
    //given
    val bmiReading = BMIReading(height = 177f, weight = 53f)
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        defaultHistoryUuid = uuidGenerator.v4(),
        patientUuid = patientUuid
    )) doReturn
        TestData.medicalHistory(isSmoking = Yes)

    whenever(patientAttributeRepository.getPatientAttributeImmediate(patientUuid)) doReturn
        TestData.patientAttribute(reading = bmiReading)

    whenever(cvdRiskRepository.getCVDRiskImmediate(patientUuid)) doReturn
        TestData.cvdRisk(riskScore = CVDRiskRange(27, 27))

    //when
    testCase.dispatch(LoadStatinInfo(patientUuid))

    //then
    testCase.assertOutgoingEvents(StatinInfoLoaded(StatinInfo(
        canPrescribeStatin = true,
        cvdRisk = CVDRiskRange(27, 27),
        isSmoker = Yes,
        bmiReading = bmiReading
    )))
  }

  @Test
  fun `when show smoking status dialog view effect is received, then show the smoking status dialog`() {
    // when
    testCase.dispatch(ShowSmokingStatusDialog)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showSmokingStatusDialog()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open BMI entry sheet view effect is received, then open the BMI entry sheet`() {
    // when
    testCase.dispatch(OpenBMIEntrySheet(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openBMIEntrySheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }
}
