package org.resolvetosavelives.red.sync.patient

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatientPushResponse(

    @Json(name = "errors")
    val validationErrors: List<ValidationErrors>
)

/**
 * Errors present in one patient.
 */
@JsonClass(generateAdapter = true)
data class ValidationErrors(

    @Json(name = "id")
    val uuid: String,

    @Json(name = "schema")
    val schemaErrorMessages: List<String>?,

    @Json(name = "age")
    val ageErrors: List<String>?
)
