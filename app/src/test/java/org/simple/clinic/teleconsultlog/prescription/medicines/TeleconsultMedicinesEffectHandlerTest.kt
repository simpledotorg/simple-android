package org.simple.clinic.teleconsultlog.prescription.medicines

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
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultMedicinesEffectHandlerTest {

  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val uiActions = mock<TeleconsultMedicinesUiActions>()
  private val effectHandler = TeleconsultMedicinesEffectHandler(
      prescriptionRepository = prescriptionRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  )
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler.build())

  private val patientUuid = UUID.fromString("62344f3e-73ec-45c7-b76e-22396cf17de8")

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load patient medicines effect is received, then load medicines`() {
    // given
    val medicines = listOf(
        TestData.prescription(
            uuid = UUID.fromString("24cd76ff-0d27-4b05-9d6b-44c12aee2416"),
            patientUuid = patientUuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("fd9cce64-d1d8-4289-ab8d-02a2b511e927"),
            patientUuid = patientUuid
        )
    )

    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn medicines

    // when
    effectHandlerTestCase.dispatch(LoadPatientMedicines(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientMedicinesLoaded(medicines))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open edit medicines effect is received, then open edit medicines`() {
    // when
    effectHandlerTestCase.dispatch(OpenEditMedicines(patientUuid))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openEditMedicines(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open drug duration sheet effect is received, then open drug duration sheet`() {
    // given
    val prescription = TestData.prescription(
        uuid = UUID.fromString("288a87b4-5835-4b4e-b9ed-abad3d8b590c"),
        name = "Taco 10mg"
    )

    // when
    effectHandlerTestCase.dispatch(OpenDrugDurationSheet(prescription))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openDrugDurationSheet(prescription)
    verifyNoMoreInteractions(uiActions)
  }
}
