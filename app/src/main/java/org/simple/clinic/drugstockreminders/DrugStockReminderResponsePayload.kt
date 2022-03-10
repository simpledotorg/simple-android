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

    @Json(name = "drugs")
    val drugs: List<DrugStockReportPayload>
)
