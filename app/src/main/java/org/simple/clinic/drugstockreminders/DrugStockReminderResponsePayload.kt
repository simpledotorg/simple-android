package org.simple.clinic.drugstockreminders

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class DrugStockReminderResponsePayload(

    @Json(name = "month")
    val month: String,

    @Json(name = "facility_id")
    val facilityUuid: UUID,

    @Json(name = "drug_stock_form_url")
    val drugStockFormUrl: String,

    @Json(name = "drugs")
    val drugs: List<DrugStockReportPayload>
)
