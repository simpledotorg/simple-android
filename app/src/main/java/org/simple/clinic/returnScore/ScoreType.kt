package org.simple.clinic.returnScore

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class ScoreType : Parcelable {
  object TypeAdapter : SafeEnumTypeAdapter<ScoreType>(
      knownMappings = mapOf(
          LikelyToReturnScoreType to "likely_to_return",
      ),
      unknownStringToEnumConverter = ::Unknown,
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): ScoreType? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(scoreType: ScoreType?): String? = TypeAdapter.fromEnum(scoreType)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): ScoreType? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(scoreType: ScoreType?): String? = TypeAdapter.fromEnum(scoreType)
  }

  companion object {
    @VisibleForTesting
    fun random(): ScoreType = TypeAdapter.knownMappings.keys.shuffled().first()
  }
}

@Parcelize
data object LikelyToReturnScoreType : ScoreType()

@Parcelize
data class Unknown(val actualValue: String) : ScoreType()

