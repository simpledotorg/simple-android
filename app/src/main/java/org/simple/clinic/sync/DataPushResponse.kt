package org.simple.clinic.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class DataPushResponse(

    @Json(name = "errors")
    val validationErrors: List<ValidationErrors>
)

/**
 * Errors present in one patient.
 */
@JsonClass(generateAdapter = true)
data class ValidationErrors(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "schema")
    val schemaErrorMessages: List<String>?
)
