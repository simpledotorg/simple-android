package org.simple.clinic.drugstockreminders

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class DrugStockReportPayload(
    @Json(name = "protocol_drug_id")
    val protocolDrugId: UUID,

    @Json(name = "in_stock")
    val drugsInStock: Int?,

    @Json(name = "received")
    val drugsReceived: Int?
)
