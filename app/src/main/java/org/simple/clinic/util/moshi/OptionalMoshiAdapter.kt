package org.simple.clinic.util.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.util.Optional
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
      value?.run {
        if (value.isPresent())
          moshi.adapter<Any>(type).toJson(writer, value.get())
        else
          writer.nullValue()
      }
    }

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Optional<*>? {
      return if (reader.peek() == JsonReader.Token.NULL) {
        Optional.empty<Any>()
      } else {
        Optional.of(moshi.adapter<Any>(type).fromJson(reader)!!)
      }
    }
  }
}
