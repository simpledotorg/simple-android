package org.simple.clinic.drugstockreminders

import java.util.UUID

data class DrugStockReport(
    val protocolDrugId: UUID,
    val drugsInStock: Int,
    val drugsReceived: Int
)
