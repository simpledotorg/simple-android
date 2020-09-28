package org.simple.clinic.teleconsultlog.prescription

import android.graphics.Bitmap
import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.signature.SignatureRepository
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.LocalDate
import java.util.UUID

class TeleconsultPrescriptionEffectHandlerTest {

  private val uiActions = mock<TeleconsultPrescriptionUiActions>()
  private val patientRepository = mock<PatientRepository>()
  private val signatureRepository = mock<SignatureRepository>()
  private val teleconsultRecordRepository = mock<TeleconsultRecordRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val medicalRegistrationIdPreference = mock<Preference<Optional<String>>>()
  private val effectHandler = TeleconsultPrescriptionEffectHandler(
      patientRepository = patientRepository,
      signatureRepository = signatureRepository,
      teleconsultRecordRepository = teleconsultRecordRepository,
      prescriptionRepository = prescriptionRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      medicalRegistrationIdPreference = medicalRegistrationIdPreference,
      uiActions = uiActions
  )
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler.build())

  private val testClock = TestUtcClock(LocalDate.parse("2018-01-01"))

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load patient details effect is received, then load patient details`() {
    // given
    val patientUuid = UUID.fromString("6e1898a7-e3c0-4497-bf26-5cabbb1cb6c8")
    val patient = TestData.patient(
        uuid = UUID.fromString("9d87d557-e092-48da-ac53-429a7f957598")
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    effectHandlerTestCase.dispatch(LoadPatientDetails(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientDetailsLoaded(patient))

    verifyZeroInteractions(uiActions)
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
  fun `when show signature effect is received, then show signature error`() {
    // when
    effectHandlerTestCase.dispatch(ShowSignatureRequiredError)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).showSignatureRequiredError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load data for next clicked is received, then load data`() {
    // given
    val teleconsultRecordId = UUID.fromString("b38e1672-c75e-4cbc-a723-f1952687703d")
    val medicalRegistrationId = "ABC123456"
    val instructions = "This is a medical instruction"

    val bitmap = mock<Bitmap>()

    whenever(signatureRepository.getSignatureBitmap()) doReturn bitmap

    // when
    effectHandlerTestCase.dispatch(LoadDataForNextClick(teleconsultRecordId, instructions, medicalRegistrationId))

    // then
    effectHandlerTestCase.assertOutgoingEvents(DataForNextClickLoaded(
        medicalInstructions = instructions,
        medicalRegistrationId = medicalRegistrationId,
        hasSignatureBitmap = true
    ))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when create prescription effect is received, then create prescription`() {
    // given
    val patientUuid = UUID.fromString("3b19894c-d0ee-483e-a64b-745cc3d52306")
    val teleconsultRecordId = UUID.fromString("9b704cfe-5d31-411d-ae3f-e1b3b29e45d9")
    val medicalRegistrationId = "ABC123456"
    val instructions = "This is a medical instruction"

    val prescribedDrug1 = TestData.prescription(
        uuid = UUID.fromString("10eb023a-cd5d-4140-9a7e-0e08ac26f3d7"),
        name = "Taco",
        patientUuid = patientUuid,
        timestamps = Timestamps.create(testClock),
        syncStatus = SyncStatus.DONE
    )

    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn listOf(prescribedDrug1)

    // when
    effectHandlerTestCase.dispatch(CreatePrescription(patientUuid, teleconsultRecordId, instructions, medicalRegistrationId))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PrescriptionCreated(instructions))

    verify(medicalRegistrationIdPreference).set(Optional.of(medicalRegistrationId))
    verifyNoMoreInteractions(medicalRegistrationIdPreference)

    verify(teleconsultRecordRepository).updateMedicalRegistrationId(teleconsultRecordId, medicalRegistrationId)
    verifyNoMoreInteractions(teleconsultRecordRepository)

    verify(prescriptionRepository).newestPrescriptionsForPatientImmediate(patientUuid)
    verify(prescriptionRepository).addTeleconsultationIdToDrugs(listOf(prescribedDrug1), teleconsultRecordId)
    verifyNoMoreInteractions(prescriptionRepository)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open share prescription screen effect is received, then open share prescription screen`() {
    // given
    val teleconsultRecordId = UUID.fromString("2e9b48fd-786e-425b-8f97-049d59db81ec")
    val medicalInstructions = "This is a medical instruction"

    // when
    effectHandlerTestCase.dispatch(OpenSharePrescriptionScreen(teleconsultRecordId, medicalInstructions))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openSharePrescriptionScreen(teleconsultRecordId, medicalInstructions)
    verifyNoMoreInteractions(uiActions)
  }
}
