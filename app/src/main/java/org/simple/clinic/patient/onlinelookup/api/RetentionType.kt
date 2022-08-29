package org.simple.clinic.patient.onlinelookup.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class RetentionType {

  @Json(name = "temporary")
  Temporary,

  @Json(name = "permanent")
  Permanent,

  // Fallback value for newly added enum types for older app versions
  Unknown
}
