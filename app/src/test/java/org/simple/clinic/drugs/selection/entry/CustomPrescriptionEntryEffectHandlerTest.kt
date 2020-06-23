package org.simple.clinic.drugs.selection.entry

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.util.UUID

class CustomPrescriptionEntryEffectHandlerTest {

  private val uiActions = mock<CustomPrescriptionEntryUiActions>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val facility = TestData.facility(uuid = UUID.fromString("e04d0b0b-3afa-46be-8705-a171817dc51c"))
  private val patientUuid = UUID.fromString("c36b7606-bc22-4193-9311-d822623fe8cf")
  private val prescriptionUuid = UUID.fromString("3c31cd9d-5172-43d8-bc28-d86b39a54ac7")
  private val effectHandlerTestCase = EffectHandlerTestCase(
      CustomPrescriptionEntryEffectHandler(
          uiActions,
          TrampolineSchedulersProvider(),
          prescriptionRepository,
          Lazy { facility },
          FakeUuidGenerator.fixed(prescriptionUuid)
      ).build()
  )

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when update prescription effect is received, then update the prescription`() {
    // given
    whenever(prescriptionRepository.softDeletePrescription(prescriptionUuid)) doReturn Completable.complete()
    whenever(prescriptionRepository.savePrescription(
        uuid = prescriptionUuid,
        patientUuid = patientUuid,
        name = "Atenolol",
        dosage = "10mg",
        rxNormCode = null,
        isProtocolDrug = false,
        facility = facility
    )) doReturn Completable.complete()

    // when
    effectHandlerTestCase.dispatch(UpdatePrescription(patientUuid, prescriptionUuid, "Atenolol", "10mg"))

    // then
    effectHandlerTestCase.assertOutgoingEvents(CustomPrescriptionSaved)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when close sheet effect is received, then close the sheet`() {
    // when
    effectHandlerTestCase.dispatch(CloseSheet)

    // then
    verify(uiActions).finish()
    verifyNoMoreInteractions(uiActions)

    effectHandlerTestCase.assertNoOutgoingEvents()
  }
}
