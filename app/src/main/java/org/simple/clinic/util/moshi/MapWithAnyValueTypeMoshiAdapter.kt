package org.simple.clinic.util.moshi

import com.squareup.moshi.FromJson

class MapWithAnyValueTypeMoshiAdapter {
  @FromJson
  fun fromJson(value: Map<String, Any?>?): Map<String, Any?> {
    val map: MutableMap<String, Any?> = mutableMapOf()

    value?.forEach {
      map[it.key] = try {
        if (it.value is Double && (it.value as Double).rem(1).equals(0.0)) {
          // This condition will convert 100.0 to 100 which is not intended for float/double values.
          // Currently, `QuestionnaireResponsePayload.kt_content` only has integer values.
          // Update this logic when float/double values get introduced.
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
