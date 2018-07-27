package org.simple.clinic.registration

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.LocalDate

@JsonClass(generateAdapter = true)
data class RegistrationRequest(

    @Json(name = "user")
    val user: RegisterUserPayload
)

@JsonClass(generateAdapter = true)
data class RegisterUserPayload(

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "phone_number")
    val phoneNumber: String,

    @Json(name = "password")
    val pin: String,

    @Json(name = "password_confirmation")
    val pinConfirmation: String,

    @Json(name = "created_at")
    val createdAt: LocalDate,

    @Json(name = "updated_at")
    val updatedAt: LocalDate
)
