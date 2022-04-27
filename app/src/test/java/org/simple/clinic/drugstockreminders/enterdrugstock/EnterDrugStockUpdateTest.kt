package org.simple.clinic.drugstockreminders.enterdrugstock

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import java.util.Optional

class EnterDrugStockUpdateTest {

  @Test
  fun `when drug stock form url is loaded, then update the model`() {
    val defaultModel = EnterDrugStockModel.create()
    val drugStockFormUrl = "drug_stock_form_url"

    UpdateSpec(EnterDrugStockUpdate())
        .given(defaultModel)
        .whenEvent(DrugStockFormUrlLoaded(Optional.of(drugStockFormUrl)))
        .then(assertThatNext(
            hasModel(defaultModel.drugStockFormUrlLoaded(drugStockFormUrl)),
            hasNoEffects()
        ))
  }
}
