package org.simple.clinic.util.moshi

import com.squareup.moshi.FromJson

class MapWithAnyValueTypeMoshiAdapter {
  @FromJson
  fun fromJson(value: Map<String, Any?>?): Map<String, Any?> {
    val map: MutableMap<String, Any?> = mutableMapOf()

    value?.forEach {
      map[it.key] = try {
        if (it.value is Double && (it.value as Double).rem(1).equals(0.0)) {
          (it.value as Double).toInt()
        } else {
          it.value
        }
      } catch (ex: Exception) {
        it.value
      }
    }
    return map.toMap()
  }
}
