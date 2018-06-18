package org.simple.clinic.drugs.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrescriptionPushRequest(

    @Json(name = "prescription_drugs")
    val prescriptions: List<PrescribedDrugPayload>
)
