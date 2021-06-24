package org.simple.clinic.patient.onlinelookup

import com.squareup.moshi.Json

enum class RetentionType {

  @Json(name = "temporary")
  Temporary,

  @Json(name = "permanent")
  Permanent
}
