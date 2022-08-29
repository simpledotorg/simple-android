package org.simple.clinic.drugstockreminders.enterdrugstock

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class EnterDrugStockInitTest {

  @Test
  fun `when screen is created, then load the drug stock form url`() {
    val defaultModel = EnterDrugStockModel.create()
    InitSpec(EnterDrugStockInit())
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadDrugStockFormUrl)
        ))
  }
}
