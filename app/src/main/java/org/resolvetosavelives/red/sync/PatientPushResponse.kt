package org.resolvetosavelives.red.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatientPushResponse(

    @Json(name = "errors")
    val validationErrors: List<ValidationErrors>
) {

  fun hasValidationErrors(): Boolean {
    return validationErrors.isNotEmpty()
  }
}

@JsonClass(generateAdapter = true)
data class ValidationErrors(

    @Json(name = "id")
    val id: String,

    @Json(name = "address")
    val addressErrors: ValidationError?,

    @Json(name = "phone_numbers")
    val phoneNumberErrors: ValidationError?
)

@JsonClass(generateAdapter = true)
data class ValidationError(

    @Json(name = "id")
    val id: String,

    @Json(name = "field_with_error")
    val fieldWithError: List<String>
)
