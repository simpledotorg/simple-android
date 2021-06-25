package org.simple.clinic.patient.onlinelookup.api

import com.squareup.moshi.Json

enum class RetentionType {

  @Json(name = "temporary")
  Temporary,

  @Json(name = "permanent")
  Permanent
}
