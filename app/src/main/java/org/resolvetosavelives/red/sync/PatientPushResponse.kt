package org.resolvetosavelives.red.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatientPushResponse(

    @Json(name = "errors")
    val errors: List<ValidationErrors>?
)

@JsonClass(generateAdapter = true)
data class ValidationErrors(

    @Json(name = "address")
    val addressErrors: ValidationError?,

    @Json(name = "phone_numbers")
    val phoneNumberErrors: ValidationError?
)

@JsonClass(generateAdapter = true)
data class ValidationError(

    @Json(name = "field_with_error")
    val fieldWithError: List<String>
)
