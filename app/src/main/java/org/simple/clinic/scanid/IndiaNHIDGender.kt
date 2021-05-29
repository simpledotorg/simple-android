package org.simple.clinic.scanid

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.simple.clinic.patient.Gender

enum class IndiaNHIDGender {
  MALE, FEMALE, UNKNOWN;

  class MoshiTypeAdapter {
    @FromJson
    fun fromJson(value: String?): IndiaNHIDGender {
      return when (value) {
        "M" -> MALE
        "F" -> FEMALE
        else -> UNKNOWN
      }
    }

    @ToJson
    fun toJson(answer: IndiaNHIDGender): String {
      return when (answer) {
        MALE -> "M"
        FEMALE -> "F"
        UNKNOWN -> "U"
      }
    }
  }
}