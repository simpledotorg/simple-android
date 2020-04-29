package org.simple.clinic.util.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional

import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class MoshiOptionalAdapterFactory : JsonAdapter.Factory {

  override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
    return if (type is ParameterizedType && type.rawType === Optional::class.java) {
      OptionalAdapter(moshi, type.actualTypeArguments[0])
    } else null
  }

  private class OptionalAdapter(val moshi: Moshi, val type: Type) : JsonAdapter<Optional<*>>() {

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: Optional<*>?) {
      when (value) {
        is Just -> moshi.adapter<Any>(type).toJson(writer, value.value)
        is None -> writer.nullValue()
      }
    }

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Optional<*>? {
      return if (reader.peek() == JsonReader.Token.NULL) {
        None
      } else {
        Just(moshi.adapter<Any>(type).fromJson(reader)!!)
      }
    }
  }
}
