package org.simple.clinic.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi

import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class MoshiOptionalAdapterFactory : JsonAdapter.Factory {

  override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
    return if (type is ParameterizedType && type.rawType === Optional::class.java) {
      OptionalAdapter(moshi, type.actualTypeArguments[0])
    } else null
  }

  private class OptionalAdapter internal constructor(private val moshi: Moshi, private val type: Type) : JsonAdapter<Optional<*>>() {

    @Throws(IOException::class)
    override fun toJson(out: JsonWriter, value: Optional<*>?) {
      when (value) {
        is Just -> moshi.adapter<Any>(type).toJson(out, value.value)
        is None -> out.nullValue()
      }
    }

    @Throws(IOException::class)
    override fun fromJson(`in`: JsonReader): Optional<*>? {
      return if (`in`.peek() == JsonReader.Token.NULL) {
        None
      } else {
        Just(moshi.adapter<Any>(type).fromJson(`in`)!!)
      }
    }
  }
}
