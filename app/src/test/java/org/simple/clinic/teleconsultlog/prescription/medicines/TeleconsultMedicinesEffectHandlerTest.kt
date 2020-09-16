package org.simple.clinic.teleconsultlog.prescription.medicines

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
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
  private val effectHandler = TeleconsultMedicinesEffectHandler(
      prescriptionRepository = prescriptionRepository,
      schedulersProvider = TestSchedulersProvider.trampoline()
  )
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load patient medicines effect is received, then load medicines`() {
    // given
    val patientUuid = UUID.fromString("62344f3e-73ec-45c7-b76e-22396cf17de8")

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
  }
}
