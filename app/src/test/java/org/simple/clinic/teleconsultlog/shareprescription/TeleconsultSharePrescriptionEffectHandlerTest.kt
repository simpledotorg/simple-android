package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap
import android.net.Uri
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
import org.simple.clinic.signature.SignatureRepository
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultSharePrescriptionEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val signatureRepository = mock<SignatureRepository>()
  private val uiActions = mock<TeleconsultSharePrescriptionUiActions>()
  private val medicalRegistrationIdPreference = mock<Preference<Optional<String>>>()
  private val medicalRegistrationId = "1111111111"
  private val teleconsultSharePrescriptionRepository = mock<TeleconsultSharePrescriptionRepository>()
  private val prescriptionBitmap = mock<Bitmap>()
  private val fileName = "Simple Prescription"
  private val imageUri = mock<Uri>()
  private val effectHandler = TeleconsultSharePrescriptionEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      prescriptionRepository = prescriptionRepository,
      signatureRepository = signatureRepository,
      uiActions = uiActions,
      medicalRegistrationId = medicalRegistrationIdPreference,
      teleconsultSharePrescriptionRepository = teleconsultSharePrescriptionRepository
  )

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler = effectHandler.build())

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load patient medicines effect is received, load the medicines`() {
    // given
    val patientUuid = UUID.fromString("b736a740-f344-4ce1-9b58-ffbc734a1c74")
    val prescriptionUuid1 = UUID.fromString("f51cdda1-e848-432f-bfcc-7078858cec71")
    val prescriptionUuid2 = UUID.fromString("8478da29-772d-4aee-a499-daa2f3035a7c")
    val medicines = listOf(
        TestData.prescription(
            uuid = prescriptionUuid1,
            patientUuid = patientUuid
        ),
        TestData.prescription(
            uuid = prescriptionUuid2,
            patientUuid = patientUuid
        )
    )

    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid = patientUuid)) doReturn medicines

    // when
    effectHandlerTestCase.dispatch(LoadPatientMedicines(patientUuid = patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientMedicinesLoaded(medicines))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when load signature effect is received, then load the signature bitmap`() {
    // given
    val signatureBitmap = mock<Bitmap>()
    whenever(signatureRepository.getSignatureBitmap()) doReturn signatureBitmap

    // when
    effectHandlerTestCase.dispatch(LoadSignature)

    // then
    effectHandlerTestCase.assertOutgoingEvents(SignatureLoaded(bitmap = signatureBitmap))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when set the signature effect is received, then set the signature`() {
    // given
    val signatureBitmap = mock<Bitmap>()

    // when
    effectHandlerTestCase.dispatch(SetSignature(signatureBitmap))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).setSignatureBitmap(bitmap = signatureBitmap)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load medical registration Id effect is received, then load the medical registration Id if it exists`() {
    // given
    whenever(medicalRegistrationIdPreference.get()) doReturn Optional.of(medicalRegistrationId)

    // when
    effectHandlerTestCase.dispatch(LoadMedicalRegistrationId)

    // then
    effectHandlerTestCase.assertOutgoingEvents(MedicalRegistrationIdLoaded(medicalRegistrationId))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when set medical registration Id effect is received, then load the medical registration Id`() {
    // when
    effectHandlerTestCase.dispatch(SetMedicalRegistrationId(medicalRegistrationId = medicalRegistrationId))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).setMedicalRegistrationId(medicalRegistrationId)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when Go to home screen effect is received, then open home screen`() {
    // when
    effectHandlerTestCase.dispatch(GoToHomeScreen)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).openHomeScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load patient profile effect is received, then load the patient profile`() {
    // given
    val patientUuid = UUID.fromString("55419722-b722-48f4-a2c1-39a8c6e8f390")
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid
    )

    whenever(patientRepository.patientProfileImmediate(patientUuid)) doReturn Optional.of(patientProfile)

    // when
    effectHandlerTestCase.dispatch(LoadPatientProfile(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientProfileLoaded(patientProfile))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when save bitmap to external storage effect is received, then store the bitmap`() {
    // given
    whenever(teleconsultSharePrescriptionRepository.savePrescriptionBitmap(prescriptionBitmap)) doReturn fileName

    // when
    effectHandlerTestCase.dispatch(SaveBitmapInExternalStorage(prescriptionBitmap))
    
    // then
    effectHandlerTestCase.assertOutgoingEvents(PrescriptionImageSaved)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when share prescription as image effect is received, then share prescription as image`() {
    // given
    whenever(teleconsultSharePrescriptionRepository.savePrescriptionBitmap(prescriptionBitmap)) doReturn fileName

    // when
    effectHandlerTestCase.dispatch(SharePrescriptionAsImage(prescriptionBitmap))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PrescriptionSavedForSharing(fileName))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when retrieve image Uri effect is received, then retrieve the image Uri`() {
    // given
    whenever(teleconsultSharePrescriptionRepository.sharePrescription(fileName)) doReturn imageUri

    // when
    effectHandlerTestCase.dispatch(RetrievePrescriptionImageUri(fileName))

    // then
    effectHandlerTestCase.assertOutgoingEvents(SharePrescriptionUri(imageUri))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open sharing dialog effect is received, then open the dialog to share the image`() {
    // when
    effectHandlerTestCase.dispatch(OpenSharingDialog(imageUri))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).sharePrescriptionAsImage(imageUri)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when go back effect is received, then go back to previous screen`() {
    // when
    effectHandlerTestCase.dispatch(GoBack)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).goToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }
}
