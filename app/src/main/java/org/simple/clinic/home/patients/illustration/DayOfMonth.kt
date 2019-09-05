package org.simple.clinic.home.patients.illustration

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.simple.clinic.util.RoomEnumTypeConverter
import org.threeten.bp.Month
import java.util.Locale

data class DayOfMonth(
    val day: Int,
    val month: Month
) {

  class MonthRoomTypeConverter : RoomEnumTypeConverter<Month>(Month::class.java)

  object MoshiTypeAdapter {
    @FromJson
    fun toModel(value: String): DayOfMonth {
      val splits = value.split(" ")
      return DayOfMonth(
          day = splits[0].toInt(),
          month = Month.valueOf(splits[1].toUpperCase(Locale.ROOT))
      )
    }

    @ToJson
    fun fromModel(dayOfMonth: DayOfMonth): String =
        "${dayOfMonth.day} ${dayOfMonth.month}"
  }
}
