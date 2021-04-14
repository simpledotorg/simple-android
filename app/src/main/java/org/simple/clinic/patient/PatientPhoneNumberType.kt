package org.simple.clinic.patient

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class PatientPhoneNumberType : Parcelable {

  @Parcelize
  object Mobile : PatientPhoneNumberType()

  @Parcelize
  object Landline : PatientPhoneNumberType()

  @Parcelize
  data class Unknown(val actualValue: String) : PatientPhoneNumberType()

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  object TypeAdapter : SafeEnumTypeAdapter<PatientPhoneNumberType>(
      knownMappings = mapOf(
          Mobile to "mobile",
          Landline to "landline"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): PatientPhoneNumberType? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(phoneNumberType: PatientPhoneNumberType): String? = TypeAdapter.fromEnum(phoneNumberType)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): PatientPhoneNumberType? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(phoneNumberType: PatientPhoneNumberType): String? = TypeAdapter.fromEnum(phoneNumberType)
  }
}
