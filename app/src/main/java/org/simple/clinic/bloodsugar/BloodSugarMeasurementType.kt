package org.simple.clinic.bloodsugar

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class BloodSugarMeasurementType : Parcelable {

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

@Parcelize
object Random : BloodSugarMeasurementType()

@Parcelize
object PostPrandial : BloodSugarMeasurementType()

@Parcelize
object Fasting : BloodSugarMeasurementType()

@Parcelize
data class Unknown(val actualValue: String) : BloodSugarMeasurementType()
