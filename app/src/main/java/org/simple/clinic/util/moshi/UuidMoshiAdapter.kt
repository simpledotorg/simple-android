package org.simple.clinic.util.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.UUID

class UuidMoshiAdapter {

  @FromJson
  fun toUuid(value: String?): UUID? {
    return value?.let { UUID.fromString(it) }
  }

  @ToJson
  fun fromUuid(uuid: UUID?): String? {
    return uuid?.toString()
  }
}
