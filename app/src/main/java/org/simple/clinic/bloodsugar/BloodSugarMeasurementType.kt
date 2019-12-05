package org.simple.clinic.bloodsugar

import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class BloodSugarMeasurementType {

  object TypeAdapter : SafeEnumTypeAdapter<BloodSugarMeasurementType>(
      knownMappings = mapOf(
          Random to "random",
          PostPrandial to "post_prandial",
          Fasting to "fasting"
      ),
      unknownStringToEnumConverter = ::Unknown,
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): BloodSugarMeasurementType? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(measurementType: BloodSugarMeasurementType?): String? = TypeAdapter.fromEnum(measurementType)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): BloodSugarMeasurementType? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(measurementType: BloodSugarMeasurementType?): String? = TypeAdapter.fromEnum(measurementType)
  }
}

object Random : BloodSugarMeasurementType()
object PostPrandial : BloodSugarMeasurementType()
object Fasting : BloodSugarMeasurementType()
data class Unknown(val actualValue: String) : BloodSugarMeasurementType()
