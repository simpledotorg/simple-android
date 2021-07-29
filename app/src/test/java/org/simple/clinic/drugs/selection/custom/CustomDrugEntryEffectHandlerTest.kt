package org.simple.clinic.drugs.selection.custom

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
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.util.UUID

class CustomDrugEntryEffectHandlerTest {
  private val uiActions = mock<CustomDrugEntrySheetUiActions>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val facility = TestData.facility(uuid = UUID.fromString("d6685d51-f882-4995-b922-a6c637eed0a5"))
  private val patientUuid = UUID.fromString("be756a9f-005b-4592-8609-209ba0a867a4")

  private val customDrugUUID = UUID.fromString("6bbc5bbe-863c-472a-b962-1fd3198e20d1")
  private val uuidGenerator = FakeUuidGenerator.fixed(customDrugUUID)

  private val effectHandler = CustomDrugEntryEffectHandler(
      TestSchedulersProvider.trampoline(),
      prescriptionRepository,
      { facility },
      uuidGenerator,
      uiActions).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when show edit frequency dialog effect is received, show edit frequency dialog`() {
    // given
    val frequency = DrugFrequency.OD

    // when
    testCase.dispatch(ShowEditFrequencyDialog(frequency))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showEditFrequencyDialog(frequency)
  }

  @Test
  fun `when add custom drug to prescription effect is received, add custom drug to prescription repository`() {
    // given
    val drugName = "Amlodipine"
    val dosage = "2.5 mg"
    val frequency = DrugFrequency.OD

    whenever(prescriptionRepository.savePrescription(uuidGenerator.v4(),
        patientUuid,
        drugName,
        dosage,
        null,
        false,
        MedicineFrequency.OD,
        facility)).thenReturn(Completable.complete())

    // when
    testCase.dispatch(SaveCustomDrugToPrescription(patientUuid, drugName, dosage, null, frequency))

    // then
    verify(prescriptionRepository).savePrescription(uuidGenerator.v4(),
        patientUuid,
        drugName,
        dosage,
        null,
        false,
        MedicineFrequency.OD,
        facility)
    verifyNoMoreInteractions(prescriptionRepository)
    testCase.assertOutgoingEvents(CustomDrugSaved)
    verifyZeroInteractions(uiActions)
  }
}
