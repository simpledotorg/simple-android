package org.simple.clinic.user

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class UserStatus: Parcelable {

  @Parcelize
  object WaitingForApproval : UserStatus()

  @Parcelize
  object ApprovedForSyncing : UserStatus()

  @Parcelize
  object DisapprovedForSyncing : UserStatus()

  @Parcelize
  data class Unknown(val actualValue: String) : UserStatus()

  object TypeAdapter : SafeEnumTypeAdapter<UserStatus>(
      knownMappings = mapOf(
          WaitingForApproval to "requested",
          ApprovedForSyncing to "allowed",
          DisapprovedForSyncing to "denied"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {
    @TypeConverter
    fun toEnum(value: String?): UserStatus? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(reason: UserStatus?): String? = TypeAdapter.fromEnum(reason)
  }

  class MoshiTypeConverter {
    @FromJson
    fun toEnum(value: String?): UserStatus? = TypeAdapter.toEnum(value)

    @ToJson
    fun fromEnum(reason: UserStatus?): String? = TypeAdapter.fromEnum(reason)
  }

  companion object {
    @VisibleForTesting
    fun random(): UserStatus = TypeAdapter.knownMappings.keys.shuffled().first()
  }
}
