package org.simple.clinic.drugstockreminders.enterdrugstock

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next
import org.simple.clinic.util.toNullable

class EnterDrugStockUpdate : Update<EnterDrugStockModel, EnterDrugStockEvent, EnterDrugStockEffect> {

  override fun update(model: EnterDrugStockModel, event: EnterDrugStockEvent): Next<EnterDrugStockModel, EnterDrugStockEffect> {
    return when (event) {
      is DrugStockFormUrlLoaded -> next(model.drugStockFormUrlLoaded(event.drugStockFormUrl.toNullable()))
    }
  }
}
