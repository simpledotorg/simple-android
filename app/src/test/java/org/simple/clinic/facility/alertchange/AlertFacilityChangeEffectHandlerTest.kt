package org.simple.clinic.facility.alertchange

import com.f2prateek.rx.preferences2.Preference
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEffect.LoadIsFacilityChangedStatus
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.IsFacilityChangedStatusLoaded
import org.simple.clinic.facility.alertchange.AlertFacilityChangeViewEffect.CloseSheetWithContinuation
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class AlertFacilityChangeEffectHandlerTest {

  private val uiActions = mock<UiActions>()
  private val isFacilitySwitchedPreference = mock<Preference<Boolean>>()
  private val effectHandlerTestCase = EffectHandlerTestCase(
      AlertFacilityChangeEffectHandler(
          isFacilitySwitchedPreference = isFacilitySwitchedPreference,
          schedulersProvider = TestSchedulersProvider.trampoline(),
          viewEffectsConsumer = AlertFacilityChangeViewEffectHandler(uiActions)::handle,
      ).build()
  )

  @Test
  fun `when load facility changed effect is received, then load if facility is changed`() {
    // given
    whenever(isFacilitySwitchedPreference.get()) doReturn true

    // when
    effectHandlerTestCase.dispatch(LoadIsFacilityChangedStatus)

    // then
    verify(isFacilitySwitchedPreference).get()
    verifyNoMoreInteractions(isFacilitySwitchedPreference)

    effectHandlerTestCase.assertOutgoingEvents(IsFacilityChangedStatusLoaded(isFacilityChanged = true))
  }

  @Test
  fun `when close sheet with continuation view effect is received, then close sheet with continuation`() {
    // when
    effectHandlerTestCase.dispatch(CloseSheetWithContinuation)

    // then
    verify(uiActions).closeSheetWithContinuation()
    verifyNoMoreInteractions(uiActions)
  }
}
