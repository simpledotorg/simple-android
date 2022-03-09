package org.simple.clinic.drugstockreminders

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DrugStockReminderRequest(

    @Json(name = "date")
    val date: String,
)
