package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Audio
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.Duration
import java.time.Instant
import java.util.UUID

class TeleconsultRecordEffectHandlerTest {

  private val teleconsultRecordId = UUID.fromString("7e9111e5-7707-434a-af36-dad313f406ee")
  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("b5a3aabe-ec33-4e0b-a7a9-b2e4bd4c4303")
  )
  private val utcClock = TestUtcClock(instant = Instant.parse("2018-01-01T00:00:00Z"))
  private val teleconsultRecordRepository = mock<TeleconsultRecordRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val uuidGenerator = mock<UuidGenerator>()
  private val uiActions = mock<UiActions>()

  private val effectHandler = TeleconsultRecordEffectHandler(
      user = { user },
      teleconsultRecordRepository = teleconsultRecordRepository,
      patientRepository = patientRepository,
      prescriptionRepository = prescriptionRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      utcClock = utcClock,
      uuidGenerator = uuidGenerator,
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when go back effect is received, then go back to previous screen`() {
    // when
    effectHandlerTestCase.dispatch(GoBack)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).goBackToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when navigate to teleconsult success screen effect is received, then navigate to teleconsult success screen`() {
    // when
    effectHandlerTestCase.dispatch(NavigateToTeleconsultSuccess)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).navigateToTeleconsultSuccessScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load teleconsult record effect is received, then load the teleconsult record`() {
    // given
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordId
    )

    whenever(teleconsultRecordRepository.getTeleconsultRecord(teleconsultRecordId)) doReturn teleconsultRecord

    // when
    effectHandlerTestCase.dispatch(LoadTeleconsultRecord(teleconsultRecordId))

    // then
    effectHandlerTestCase.assertOutgoingEvents(TeleconsultRecordLoaded(teleconsultRecord))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when create teleconsult record effect is received, then create the teleconsult record`() {
    // given
    val patientUuid = UUID.fromString("cd8c77c4-67de-482a-bfd0-2b035291b45d")
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = user.uuid,
        teleconsultRecordInfo = TestData.teleconsultRecordInfo(
            recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
            teleconsultationType = Audio,
            patientTookMedicines = Yes,
            patientConsented = Yes,
            medicalOfficerNumber = null
        ),
        teleconsultRequestInfo = null,
        createdAt = Instant.now(utcClock),
        updatedAt = Instant.now(utcClock)
    )

    // when
    effectHandlerTestCase.dispatch(CreateTeleconsultRecord(
        teleconsultRecordId = teleconsultRecordId,
        patientUuid = patientUuid,
        teleconsultationType = Audio,
        patientTookMedicine = Yes,
        patientConsented = Yes
    ))

    // then
    effectHandlerTestCase.assertOutgoingEvents(TeleconsultRecordCreated)

    verify(teleconsultRecordRepository).createTeleconsultRecordForMedicalOfficer(
        teleconsultRecordId = teleconsultRecord.id,
        patientUuid = teleconsultRecord.patientId,
        medicalOfficerId = teleconsultRecord.medicalOfficerId,
        teleconsultRecordInfo = teleconsultRecord.teleconsultRecordInfo!!
    )
    verifyNoMoreInteractions(teleconsultRecordRepository)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when load patient details effect is received, then load the patient details`() {
    // given
    val patientUuid = UUID.fromString("faacb4f7-50c9-480d-b154-6314a5e67d63")
    val patient = TestData.patient(
        uuid = patientUuid,
        fullName = "Peter Parker"
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    effectHandlerTestCase.dispatch(LoadPatientDetails(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientDetailsLoaded(patient))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when show teleconsult not recorded warning effect is received, then show the teleconsult not recorded warning`() {
    // when
    effectHandlerTestCase.dispatch(ShowTeleconsultNotRecordedWarning)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).showTeleconsultNotRecordedWarning()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when validate teleconsult record effect is received, then check if the teleconsult record exists`() {
    // given
    val teleconsultRecordId = UUID.fromString("9b0cf077-ef4a-449a-9588-db115dc7eb7c")
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordId
    )

    whenever(teleconsultRecordRepository.getTeleconsultRecord(teleconsultRecordId)) doReturn teleconsultRecord

    // when
    effectHandlerTestCase.dispatch(ValidateTeleconsultRecord(teleconsultRecordId))

    // then
    effectHandlerTestCase.assertOutgoingEvents(TeleconsultRecordValidated(true))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when clone patient prescriptions effect is received, then clone patient prescriptions`() {
    // given
    val durationToAdvanceBy = Duration.ofMinutes(20)
    val patientUuid = UUID.fromString("7bb4616b-1542-46ad-93d5-9d03b619aa99")
    val facilityUuid = UUID.fromString("32dc8f5c-47b1-453a-b1d9-aa9230630c86")

    val clonedDrug1Uuid = UUID.fromString("6d13e4f4-c0ee-4dc8-985b-e7cd82c69ffa")
    val clonedDrug2Uuid = UUID.fromString("348f864b-cec6-4823-9dc0-e9dd4dfe5379")

    val drug1 = TestData.prescription(
        uuid = UUID.fromString("95ec779b-f862-4799-92c9-f9d1899af59a"),
        name = "Drug 1",
        dosage = "10 mg",
        rxNormCode = "rx-norm-code",
        isDeleted = false,
        isProtocolDrug = false,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        syncStatus = SyncStatus.DONE,
        timestamps = Timestamps.create(utcClock),
        frequency = MedicineFrequency.TDS,
        durationInDays = 30,
        teleconsultationId = UUID.fromString("d0e47d72-6773-4333-8447-bbd1c5004088")
    )
    val drug2 = TestData.prescription(
        uuid = UUID.fromString("b0a39aba-42a1-447b-922a-6223dacf6868"),
        name = "Drug 2",
        dosage = "20 mg",
        rxNormCode = "rx-norm-code",
        isDeleted = false,
        isProtocolDrug = false,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        syncStatus = SyncStatus.DONE,
        timestamps = Timestamps.create(utcClock),
        frequency = MedicineFrequency.TDS,
        durationInDays = 30,
        teleconsultationId = UUID.fromString("d0e47d72-6773-4333-8447-bbd1c5004088")
    )
    val prescriptions = listOf(drug1, drug2)

    val clonedPrescriptions = listOf(
        drug1.copy(
            uuid = clonedDrug1Uuid,
            syncStatus = SyncStatus.PENDING,
            timestamps = drug1.timestamps.copy(
                createdAt = drug1.createdAt.plus(durationToAdvanceBy),
                updatedAt = drug1.updatedAt.plus(durationToAdvanceBy),
                deletedAt = null
            ),
            frequency = null,
            durationInDays = null,
            teleconsultationId = teleconsultRecordId
        ),
        drug2.copy(
            uuid = clonedDrug2Uuid,
            syncStatus = SyncStatus.PENDING,
            timestamps = drug2.timestamps.copy(
                createdAt = drug2.createdAt.plus(durationToAdvanceBy),
                updatedAt = drug2.updatedAt.plus(durationToAdvanceBy),
                deletedAt = null
            ),
            frequency = null,
            durationInDays = null,
            teleconsultationId = teleconsultRecordId
        )
    )

    whenever(uuidGenerator.v4()).thenReturn(clonedDrug1Uuid, clonedDrug2Uuid)
    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn prescriptions
    whenever(prescriptionRepository.save(clonedPrescriptions)) doReturn Completable.complete()

    // when
    utcClock.advanceBy(durationToAdvanceBy)
    effectHandlerTestCase.dispatch(ClonePatientPrescriptions(patientUuid, teleconsultRecordId))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientPrescriptionsCloned)

    verify(prescriptionRepository).newestPrescriptionsForPatientImmediate(patientUuid)
    verify(prescriptionRepository).softDeletePrescriptions(prescriptions)
    verify(prescriptionRepository).save(clonedPrescriptions)
    verifyNoMoreInteractions(prescriptionRepository)

    verifyZeroInteractions(uiActions)
  }
}
