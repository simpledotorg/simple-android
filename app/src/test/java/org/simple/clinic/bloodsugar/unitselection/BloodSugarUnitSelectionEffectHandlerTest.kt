package org.simple.clinic.bloodsugar.unitselection

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class BloodSugarUnitSelectionEffectHandlerTest {

  private val bloodSugarUnitPreference = mock<Preference<BloodSugarUnitPreference>>()
  private val effectHandler = BloodSugarUnitSelectionEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      bloodSugarUnitPreference = bloodSugarUnitPreference
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when update blood sugar unit selection effect is received, then update the blood sugar unit preference`() {
    // given
    val bloodSugarUnitPreferenceValue = BloodSugarUnitPreference.Mmol

    // when
    effectHandlerTestCase.dispatch(SaveBloodSugarUnitSelection(bloodSugarUnitSelection = bloodSugarUnitPreferenceValue))

    // then
    verify(bloodSugarUnitPreference).set(bloodSugarUnitPreferenceValue)
    effectHandlerTestCase.assertOutgoingEvents(BloodSugarUnitSelectionUpdated)
  }
}
