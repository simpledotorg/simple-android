package org.simple.clinic.patient

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class PatientStatus : Parcelable {

  @Parcelize
  data object Active : PatientStatus()

  @Parcelize
  data object Dead : PatientStatus()

  @Parcelize
  data object Migrated : PatientStatus()

  @Parcelize
  data object Unresponsive : PatientStatus()

  @Parcelize
  data object Inactive : PatientStatus()

  @Parcelize
  data class Unknown(val actualValue: String) : PatientStatus()

  object TypeAdapter : SafeEnumTypeAdapter<PatientStatus>(
      knownMappings = mapOf(
          Active to "active",
          Dead to "dead",
          Migrated to "migrated",
          Unresponsive to "unresponsive",
          Inactive to "inactive"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): PatientStatus? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(status: PatientStatus?): String? = TypeAdapter.fromEnum(status)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): PatientStatus? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(status: PatientStatus?): String? = TypeAdapter.fromEnum(status)
  }

  companion object {

    @VisibleForTesting
    fun random() = TypeAdapter.knownMappings.keys.shuffled().first()
  }

}
