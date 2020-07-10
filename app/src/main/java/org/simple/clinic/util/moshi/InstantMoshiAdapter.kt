package org.simple.clinic.util.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

class InstantMoshiAdapter {

  @FromJson
  fun toInstant(value: String?): Instant? {
    return value?.let {
      return Instant.parse(value)
    }
  }

  @ToJson
  fun fromInstant(instant: Instant?): String? {
    return instant?.toString()
  }
}
