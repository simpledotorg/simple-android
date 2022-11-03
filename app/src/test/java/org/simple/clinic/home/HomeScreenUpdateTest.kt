package org.simple.clinic.home

import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import java.util.Optional
import org.junit.Test
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED

class HomeScreenUpdateTest {

  @Test
  fun `when notification permission is granted, then do nothing`() {
    val defaultModel = HomeScreenModel.create()
    UpdateSpec(HomeScreenUpdate())
        .given(defaultModel)
        .whenEvent(RequestNotificationPermission(permission = Optional.of(GRANTED)))
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
        ))
  }

  @Test
  fun `when notification permission is not granted, then do nothing`() {
    val defaultModel = HomeScreenModel.create()
    UpdateSpec(HomeScreenUpdate())
        .given(defaultModel)
        .whenEvent(RequestNotificationPermission(permission = Optional.of(DENIED)))
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
        ))
  }
}
