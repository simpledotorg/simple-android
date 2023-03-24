package org.simple.clinic.util.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import javax.inject.Inject

class MapRoomTypeConverter @Inject constructor() {
  @TypeConverter
  fun fromMap(value: Map<String, Any?>): String {
    return getGson().toJson(value)
  }

  @TypeConverter
  fun toMap(value: String): Map<String, Any?> {
    val mapType: Type = object : TypeToken<Map<String?, Any?>?>() {}.type
    return getGson().fromJson(value, mapType)
  }

  private fun getGson(): Gson {
    return GsonBuilder()
        .serializeNulls()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()
  }
}
