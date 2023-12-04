package org.simple.clinic.login.applock

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class AppLockUpdateTest {

  @Test
  fun `when pin is authenticated, then load data protection consent`() {
    val defaultModel = AppLockModel.create()

    UpdateSpec(AppLockUpdate())
        .given(defaultModel)
        .whenEvent(AppLockPinAuthenticated)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadDataProtectionConsent)
        ))
  }
}
