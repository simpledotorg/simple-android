package org.simple.clinic.patient.onlinelookup.api


import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.time.Duration

class DurationFromSecondsMoshiAdapter : JsonAdapter<SecondsDuration>() {

  override fun fromJson(reader: JsonReader): SecondsDuration? {
    if (reader.peek() == JsonReader.Token.NULL) {
      reader.nextNull<Unit>()
      return null
    }
    return SecondsDuration(Duration.ofSeconds(reader.nextLong()))
  }

  override fun toJson(writer: JsonWriter, value: SecondsDuration?) {
    if (value == null) {
      writer.nullValue()
    } else {
      writer.value(value.value.seconds)
    }
  }
}

