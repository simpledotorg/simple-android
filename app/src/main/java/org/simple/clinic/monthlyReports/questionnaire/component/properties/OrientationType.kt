package org.simple.clinic.monthlyReports.questionnaire.component.properties

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class OrientationType : Parcelable {

  object TypeAdapter : SafeEnumTypeAdapter<OrientationType>(
      knownMappings = mapOf(
          Horizontal to "horizontal",
          Vertical to "vertical",
      ),
      unknownStringToEnumConverter = ::Unknown,
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): OrientationType? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(orientationType: OrientationType?): String? = TypeAdapter.fromEnum(orientationType)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): OrientationType? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(orientationType: OrientationType?): String? = TypeAdapter.fromEnum(orientationType)
  }

  companion object {
    @VisibleForTesting
    fun random(): OrientationType = TypeAdapter.knownMappings.keys.shuffled().first()
  }
}

@Parcelize
object Horizontal : OrientationType()

@Parcelize
object Vertical : OrientationType()

@Parcelize
data class Unknown(val actualValue: String) : OrientationType()
