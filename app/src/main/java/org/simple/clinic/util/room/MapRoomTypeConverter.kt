package org.simple.clinic.util.room

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import javax.inject.Inject

class MapRoomTypeConverter @Inject constructor() {
  @TypeConverter
  fun fromMap(value: Map<String, Any?>): String {
    val gson = GsonBuilder()
        .serializeNulls()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()
    return gson.toJson(value)
  }

  @TypeConverter
  fun toMap(value: String): Map<String, Any?> {
    val mapType: Type = object : TypeToken<Map<String?, Any?>?>() {}.type
    return GsonBuilder()
        .serializeNulls()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create().fromJson(value, mapType)
  }
}
