package org.simple.clinic.patient

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class DeletedReason : Parcelable {

  @Parcelize
  object Duplicate : DeletedReason()

  @Parcelize
  object AccidentalRegistration : DeletedReason()

  @Parcelize
  data class Unknown(val actualValue: String) : DeletedReason()

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  object TypeAdapter : SafeEnumTypeAdapter<DeletedReason>(
      knownMappings = mapOf(
          Duplicate to "duplicate",
          AccidentalRegistration to "accidental_registration"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): DeletedReason? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(reason: DeletedReason?): String? = TypeAdapter.fromEnum(reason)
  }

  class MoshiTypeConverter {

    @FromJson
    fun toEnum(value: String?): DeletedReason? = TypeAdapter.toEnum(value)

    @ToJson
    fun fromEnum(reason: DeletedReason): String? = TypeAdapter.fromEnum(reason)
  }
}
