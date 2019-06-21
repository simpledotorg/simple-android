package org.simple.clinic.user

import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.simple.clinic.util.SafeEnumTypeAdapter

sealed class UserStatus {

  object WaitingForApproval : UserStatus()

  object ApprovedForSyncing : UserStatus()

  object DisapprovedForSyncing : UserStatus()

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
