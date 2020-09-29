package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.summary.PatientTeleconsultationInfo
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ContactDoctorEffectHandlerTest {

  private val teleconsultRecordId = UUID.fromString("06301354-6492-4e57-bcd7-09f7a9eb7860")
  private val uuidGenerator = FakeUuidGenerator.fixed(teleconsultRecordId)
  private val teleconsultationFacilityRepository = mock<TeleconsultationFacilityRepository>()
  private val teleconsultRecordRepository = mock<TeleconsultRecordRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val medicalHistoryRepository = mock<MedicalHistoryRepository>()
  private val numberOfMeasurementsForTeleconsultation = 3
  private val patientSummaryConfig = PatientSummaryConfig(
      bpEditableDuration = Duration.ofMinutes(30),
      numberOfMeasurementsForTeleconsultation = numberOfMeasurementsForTeleconsultation
  )
  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("bd0d2249-5a40-4782-b88a-4e05c24a50a5"),
      phone = "+912222222222"
  )
  private val facility = TestData.facility(
      uuid = UUID.fromString("2b7a809c-70de-40d3-a77f-d6bffcba3cfe")
  )
  private val clock = TestUtcClock(LocalDate.parse("2018-01-01"))
  private val uiActions = mock<ContactDoctorUiActions>()
  private val effectHandler = ContactDoctorEffectHandler(
      currentUser = { user },
      currentFacility = { facility },
      teleconsultationFacilityRepository = teleconsultationFacilityRepository,
      teleconsultRecordRepository = teleconsultRecordRepository,
      patientRepository = patientRepository,
      bloodPressureRepository = bloodPressureRepository,
      bloodSugarRepository = bloodSugarRepository,
      prescriptionRepository = prescriptionRepository,
      medicalHistoryRepository = medicalHistoryRepository,
      patientSummaryConfig = patientSummaryConfig,
      uuidGenerator = uuidGenerator,
      clock = clock,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load medical officers effect is received, then load medical officers for the current facility`() {
    // given
    val medicalOfficers = listOf(
        TestData.medicalOfficer(
            id = UUID.fromString("6240e551-9b50-46ca-9de7-23d9bc66afe1"),
            fullName = "Dr Sunil Gupta",
            phoneNumber = "+911111111111"
        ),
        TestData.medicalOfficer(
            id = UUID.fromString("44fb8a5e-e737-46a5-9d5f-bffa4e513ff7"),
            fullName = "Dr Mahesh Bhatt",
            phoneNumber = "+912222222222"
        )
    )
    whenever(teleconsultationFacilityRepository.medicalOfficersForFacility(facility.uuid)) doReturn medicalOfficers

    // when
    effectHandlerTestCase.dispatch(LoadMedicalOfficers)

    // then
    effectHandlerTestCase.assertOutgoingEvents(MedicalOfficersLoaded(medicalOfficers))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when create teleconsult request for nurse effect is received, then create the teleconsult request`() {
    // given
    val patientUuid = UUID.fromString("ca25eef5-687c-408a-83c9-83c68a3f3986")
    val medicalOfficerId = UUID.fromString("f403696a-db38-4058-9b51-37f0db07207b")
    val doctorPhoneNumber = "+911111111111"

    val teleconsultRequestInfo = TestData.teleconsultRequestInfo(
        requesterId = user.uuid,
        facilityId = facility.uuid,
        requestedAt = Instant.now(clock)
    )

    // when
    effectHandlerTestCase.dispatch(CreateTeleconsultRequest(patientUuid, medicalOfficerId, doctorPhoneNumber))

    // then
    effectHandlerTestCase.assertOutgoingEvents(TeleconsultRequestCreated(teleconsultRecordId, doctorPhoneNumber))

    verify(teleconsultRecordRepository).createTeleconsultRequestForNurse(
        teleconsultRecordId = teleconsultRecordId,
        patientUuid = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = teleconsultRequestInfo
    )
    verifyNoMoreInteractions(teleconsultRecordRepository)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when load patient teleconsult info effect is received, then load patient teleconsult info`() {
    // given
    val patientUuid = UUID.fromString("71aac9c0-5be6-40f3-a09c-7e68c132bfba")
    val doctorPhoneNumber = "+911111111111"

    val bpPassport = TestData.businessId(
        uuid = UUID.fromString("7ebf2c49-8018-41b9-af7c-43afe02483d4"),
        patientUuid = patientUuid,
        identifier = Identifier("1234567", Identifier.IdentifierType.BpPassport),
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

    val medicalHistoryUuid = UUID.fromString("4d72f266-0da5-4418-82e7-238f2fcabcb3")
    val medicalHistory = TestData.medicalHistory(
        uuid = medicalHistoryUuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = Answer.Yes,
        hasDiabetes = Answer.No
    )

    val patientTeleconsultInfo = PatientTeleconsultationInfo(
        patientUuid = patientUuid,
        teleconsultRecordId = teleconsultRecordId,
        bpPassport = bpPassport.identifier.displayValue(),
        facility = facility,
        bloodPressures = bloodPressures,
        bloodSugars = bloodSugars,
        prescriptions = prescriptions,
        medicalHistory = medicalHistory,
        nursePhoneNumber = user.phoneNumber,
        doctorPhoneNumber = doctorPhoneNumber
    )

    val uuidGenerator = FakeUuidGenerator.fixed(medicalHistoryUuid)
    val effectHandler = ContactDoctorEffectHandler(
        currentUser = { user },
        currentFacility = { facility },
        teleconsultationFacilityRepository = teleconsultationFacilityRepository,
        teleconsultRecordRepository = teleconsultRecordRepository,
        patientRepository = patientRepository,
        bloodPressureRepository = bloodPressureRepository,
        bloodSugarRepository = bloodSugarRepository,
        prescriptionRepository = prescriptionRepository,
        medicalHistoryRepository = medicalHistoryRepository,
        patientSummaryConfig = patientSummaryConfig,
        uuidGenerator = uuidGenerator,
        clock = clock,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    ).build()
    val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

    whenever(patientRepository.bpPassportForPatient(patientUuid)) doReturn Optional.of(bpPassport)
    whenever(bloodPressureRepository.newestMeasurementsForPatientImmediate(patientUuid, patientSummaryConfig.numberOfMeasurementsForTeleconsultation)) doReturn bloodPressures
    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn prescriptions
    whenever(bloodSugarRepository.latestMeasurementsImmediate(patientUuid, patientSummaryConfig.numberOfMeasurementsForTeleconsultation)) doReturn bloodSugars
    whenever(medicalHistoryRepository.historyForPatientOrDefaultImmediate(medicalHistoryUuid, patientUuid)) doReturn medicalHistory

    // when
    effectHandlerTestCase.dispatch(LoadPatientTeleconsultInfo(patientUuid, teleconsultRecordId, doctorPhoneNumber))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientTeleconsultInfoLoaded(patientTeleconsultInfo))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when send teleconsult message effect is received, then send teleconsult message`() {
    // given
    val patientUuid = UUID.fromString("71aac9c0-5be6-40f3-a09c-7e68c132bfba")
    val doctorPhoneNumber = "+911111111111"

    val bpPassport = TestData.businessId(
        uuid = UUID.fromString("7ebf2c49-8018-41b9-af7c-43afe02483d4"),
        patientUuid = patientUuid,
        identifier = Identifier("1234567", Identifier.IdentifierType.BpPassport),
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

    val medicalHistoryUuid = UUID.fromString("4d72f266-0da5-4418-82e7-238f2fcabcb3")
    val medicalHistory = TestData.medicalHistory(
        uuid = medicalHistoryUuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = Answer.Yes,
        hasDiabetes = Answer.No
    )

    val patientTeleconsultInfo = PatientTeleconsultationInfo(
        patientUuid = patientUuid,
        teleconsultRecordId = teleconsultRecordId,
        bpPassport = bpPassport.identifier.displayValue(),
        facility = facility,
        bloodPressures = bloodPressures,
        bloodSugars = bloodSugars,
        prescriptions = prescriptions,
        medicalHistory = medicalHistory,
        nursePhoneNumber = user.phoneNumber,
        doctorPhoneNumber = doctorPhoneNumber
    )

    // when
    effectHandlerTestCase.dispatch(SendTeleconsultMessage(patientTeleconsultInfo, MessageTarget.SMS))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).sendTeleconsultMessage(patientTeleconsultInfo, MessageTarget.SMS)
    verifyNoMoreInteractions(uiActions)
  }
}
