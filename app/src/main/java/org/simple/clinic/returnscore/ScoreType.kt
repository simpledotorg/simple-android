package org.simple.clinic.returnscore

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
          LikelyToReturnIfCalledScoreType to "likely_to_return_if_called",
          LikelyToReturnIfNotCalledIn15DaysScoreType to "likely_to_return_if_not_called_in_15_days",
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
data object LikelyToReturnIfCalledScoreType : ScoreType()

@Parcelize
data object LikelyToReturnIfNotCalledIn15DaysScoreType : ScoreType()


@Parcelize
data class Unknown(val actualValue: String) : ScoreType()

