package org.simple.clinic.scanid

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class IndiaNHIDDateOfBirthMoshiAdapter : JsonAdapter<IndiaNHIDDateOfBirth>() {

  private val formatter =
      DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ENGLISH)

  override fun fromJson(reader: JsonReader): IndiaNHIDDateOfBirth? {
    if (reader.peek() == JsonReader.Token.NULL) {
      reader.nextNull<Unit>()
      return null
    }
    val value = reader.nextString()
    return IndiaNHIDDateOfBirth(formatter.parse(value, LocalDate::from))
  }

  override fun toJson(writer: JsonWriter, value: IndiaNHIDDateOfBirth?) {
    if (value == null) {
      writer.nullValue()
    } else {
      writer.value(value.value.format(formatter))
    }
  }
}

