package org.simple.clinic.drugs.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class PrescriptionPullResponse(

    @Json(name = "prescription_drugs")
    val prescriptions: List<PrescribedDrugPayload>,

    @Json(name = "processed_since")
    val processedSinceTimestamp: Instant
)
