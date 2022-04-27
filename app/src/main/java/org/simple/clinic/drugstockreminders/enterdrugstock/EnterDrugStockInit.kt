package org.simple.clinic.drugstockreminders.enterdrugstock

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class EnterDrugStockInit : Init<EnterDrugStockModel, EnterDrugStockEffect> {

  override fun init(model: EnterDrugStockModel): First<EnterDrugStockModel, EnterDrugStockEffect> {
    return first(model, LoadDrugStockFormUrl)
  }
}
