package org.simple.clinic.util.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.net.URI

class URIMoshiAdapter {

  @FromJson
  fun toURI(value: String?): URI? {
    return value?.let { URI.create(it) }
  }

  @ToJson
  fun fromURI(uri: URI?): String? {
    return uri?.toString()
  }
}
