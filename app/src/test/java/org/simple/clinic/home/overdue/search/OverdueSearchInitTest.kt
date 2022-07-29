package org.simple.clinic.home.overdue.search

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class OverdueSearchInitTest {

  @Test
  fun `when overdue search screen is created, then load search history and selected overdue appointment ids`() {
    val defaultModel = OverdueSearchModel.create()

    InitSpec(OverdueSearchInit(false))
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadOverdueSearchHistory, LoadSelectedOverdueAppointmentIds)
        ))
  }

  @Test
  fun `when overdue search screen is created and overdue search v2 is enabled, then load villages and patient names`() {
    val defaultModel = OverdueSearchModel.create()

    InitSpec(OverdueSearchInit(true))
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadOverdueSearchHistory, LoadSelectedOverdueAppointmentIds, LoadVillageAndPatientNames)
        ))
  }
}
