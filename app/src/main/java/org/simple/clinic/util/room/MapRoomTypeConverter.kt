package org.simple.clinic.util.room

import androidx.room.TypeConverter
import java.util.TreeMap
import javax.inject.Inject

class MapRoomTypeConverter @Inject constructor() {
  @TypeConverter
  fun fromMap(value: Map<String, String>): String {
    val sortedMap = TreeMap(value)
    return sortedMap.keys.joinToString(separator = ",").plus("<divider>")
        .plus(sortedMap.values.joinToString(separator = ","))
  }

  @TypeConverter
  fun toMap(value: String): Map<String, String> {
    return value.split("<divider>").run {
      val keys = getOrNull(0)?.split(",")?.map { it }
      val values = getOrNull(1)?.split(",")?.map { it }

      val res = hashMapOf<String, String>()
      keys?.forEachIndexed { index, s ->
        res[s] = values?.getOrNull(index) ?: ""
      }
      res
    }
  }
}
