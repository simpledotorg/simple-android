package org.simple.clinic.bloodsugar.unitselection

import com.f2prateek.rx.preferences2.Preference
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class BloodSugarUnitSelectionEffectHandlerTest {

  private val bloodSugarUnitPreference = mock<Preference<BloodSugarUnitPreference>>()
  private val uiActions = mock<BloodSugarUnitSelectionUiActions>()
  private val viewEffectHandler = BloodSugarUnitSelectionViewEffectHandler(uiActions)
  private val effectHandler = BloodSugarUnitSelectionEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      bloodSugarUnitPreference = bloodSugarUnitPreference,
      viewEffectsConsumer = viewEffectHandler::handle
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)
  private val bloodSugarUnitPreferenceValue = BloodSugarUnitPreference.Mmol


  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when update blood sugar unit selection effect is received, then update the blood sugar unit preference`() {
    // when
    effectHandlerTestCase.dispatch(SaveBloodSugarUnitSelection(bloodSugarUnitSelection = bloodSugarUnitPreferenceValue))

    // then
    verify(bloodSugarUnitPreference).set(bloodSugarUnitPreferenceValue)
    effectHandlerTestCase.assertOutgoingEvents(BloodSugarUnitSelectionUpdated)
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when close dialog effect is received, then close the dialog`() {
    // when
    effectHandlerTestCase.dispatch(CloseDialog)

    // then
    verify(uiActions).closeDialog()
    verifyNoMoreInteractions(uiActions)
    effectHandlerTestCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when prefill blood sugar unit selected effect is received, then select the prefilled unit preference`() {
    // when
    effectHandlerTestCase.dispatch(PreFillBloodSugarUnitSelected(bloodSugarUnitPreferenceValue))

    // then
    verify(uiActions).prefillBloodSugarUnitSelection(bloodSugarUnitPreferenceValue)
    verifyNoMoreInteractions(uiActions)
    effectHandlerTestCase.assertNoOutgoingEvents()
  }
}
