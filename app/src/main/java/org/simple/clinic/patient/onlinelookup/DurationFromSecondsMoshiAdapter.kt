package org.simple.clinic.patient.onlinelookup

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Duration

class DurationFromSecondsMoshiAdapter {

  @ToJson
  fun toJson(@SecondsDuration duration: Duration?): Int? = duration?.seconds?.toInt()

  @FromJson
  @SecondsDuration
  fun fromJson(seconds: Int?): Duration? = seconds?.let { Duration.ofSeconds(it.toLong()) }
}
