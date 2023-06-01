package org.simple.clinic.teleconsultlog.medicinefrequency

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.verifyNoMoreInteractions
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class MedicineFrequencyEffectHandlerTest {
  private val uiActions = mock<MedicineFrequencySheetUiActions>()
  private val effectHandler = MedicineFrequencyEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)
  private val medicineFrequency = MedicineFrequency.BD

  @After
  fun teardown() {
    testCase.dispose()
  }

  @Test
  fun `when load default medicine information effect is received, then load medicine frequency`() {
    // when
    testCase.dispatch(SetMedicineFrequency(medicineFrequency = medicineFrequency))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).setMedicineFrequency(medicineFrequency)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when save medicine frequency effect is received, then save the medicine frequency`() {
    // when
    testCase.dispatch(SaveMedicineFrequency(medicineFrequency))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).saveMedicineFrequency(medicineFrequency)
    verifyNoMoreInteractions(uiActions)
  }

}
