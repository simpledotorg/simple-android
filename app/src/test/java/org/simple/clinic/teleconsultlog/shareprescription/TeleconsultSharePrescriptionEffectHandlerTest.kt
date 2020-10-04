package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap
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
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultSharePrescriptionEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val signatureRepository = mock<SignatureRepository>()
  private val uiActions = mock<TeleconsultSharePrescriptionUiActions>()
  private val effectHandler = TeleconsultSharePrescriptionEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      prescriptionRepository = prescriptionRepository,
      signatureRepository = signatureRepository,
      uiActions = uiActions
  )

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler = effectHandler.build())

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load patient details effect is received, then load the patient details`() {
    // given
    val patientUuid = UUID.fromString("1cfd240c-0a05-41e2-bfa0-b20fe807aca8")
    val patient = TestData.patient(
        uuid = patientUuid
    )
    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    effectHandlerTestCase.dispatch(LoadPatientDetails(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientDetailsLoaded(patient))
    verifyZeroInteractions(uiActions)
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


}
