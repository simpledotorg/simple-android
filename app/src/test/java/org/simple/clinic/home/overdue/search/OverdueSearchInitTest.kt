package org.simple.clinic.home.overdue.search

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class OverdueSearchInitTest {

  @Test
  fun `when overdue search screen is created, then load search history`() {
    val defaultModel = OverdueSearchModel.create()

    InitSpec(OverdueSearchInit())
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadOverdueSearchHistory)
        ))
  }
}
