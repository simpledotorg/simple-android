package org.simple.clinic.login.applock

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class AppLockUpdateTest {

  private val defaultModel = AppLockModel.create()
  private val updateSpec = UpdateSpec(AppLockUpdate())

  @Test
  fun `when pin is authenticated, then load data protection consent`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(AppLockPinAuthenticated)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadDataProtectionConsent)
        ))
  }

  @Test
  fun `when data protection consent is loaded and consent is provided, then update model and unlock on authentication`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DataProtectionConsentLoaded(hasUserConsentedToDataProtection = true))
        .then(assertThatNext(
            hasModel(defaultModel.copy(hasUserConsentedToDataProtection = true)),
            hasEffects(UnlockOnAuthentication)
        ))
  }

  @Test
  fun `when data protection consent is loaded and consent is not provided, then update model and show data protection consent dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DataProtectionConsentLoaded(hasUserConsentedToDataProtection = false))
        .then(assertThatNext(
            hasModel(defaultModel.copy(hasUserConsentedToDataProtection = false)),
            hasEffects(ShowDataProtectionConsentDialog)
        ))
  }

  @Test
  fun `when data protection consent accept button is clicked, then mark data protection consent`() {
    updateSpec
        .given(defaultModel.copy(hasUserConsentedToDataProtection = false))
        .whenEvent(AcceptDataProtectionConsentClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkDataProtectionConsent)
        ))
  }
}
