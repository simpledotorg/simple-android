package org.simple.clinic.drugstockreminders.enterdrugstock

import java.util.Optional

sealed class EnterDrugStockEvent

data class DrugStockFormUrlLoaded(val drugStockFormUrl: Optional<String>) : EnterDrugStockEvent()
