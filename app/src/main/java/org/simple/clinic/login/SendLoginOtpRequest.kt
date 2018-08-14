package org.simple.clinic.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class SendLoginOtpRequest(

    @Json(name = "pin")
    val pin: String,

    @Json(name = "uid")
    val userId: UUID
)
