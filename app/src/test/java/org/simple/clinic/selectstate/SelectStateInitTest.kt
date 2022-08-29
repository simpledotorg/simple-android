package org.simple.clinic.selectstate

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.sharedTestCode.TestData

class SelectStateInitTest {
  private val initSpec = InitSpec(SelectStateInit())
  private val defaultModel = SelectStateModel.create()

  @Test
  fun `when screen is created, then load states`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadStates)
        ))
  }

  @Test
  fun `when screen is restored, then don't load states`() {
    val states = listOf(
        TestData.state(displayName = "Andhra Pradesh"),
        TestData.state(displayName = "Kerala")
    )
    val statesLoadedModel = SelectStateModel
        .create()
        .statesLoaded(states)

    initSpec
        .whenInit(statesLoadedModel)
        .then(assertThatFirst(
            hasModel(statesLoadedModel),
            hasNoEffects()
        ))
  }
}
