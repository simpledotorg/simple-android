package org.simple.clinic.patient.businessid

import androidx.annotation.VisibleForTesting
import androidx.room.ColumnInfo
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.simple.clinic.util.SafeEnumTypeAdapter

data class Identifier(

    @ColumnInfo(name = "identifier")
    val value: String,

    @ColumnInfo(name = "identifierType")
    val type: IdentifierType
) {
  sealed class IdentifierType {

    object BpPassport : IdentifierType()

    data class Unknown(val actual: String) : IdentifierType()

    object TypeAdapter : SafeEnumTypeAdapter<IdentifierType>(
        knownMappings = mapOf(
            BpPassport to "simple_bp_passport"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actual }
    )

    class RoomTypeConverter {

      @TypeConverter
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(enum: IdentifierType?) = TypeAdapter.fromEnum(enum)
    }

    class MoshiTypeAdapter {

      @FromJson
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @ToJson
      fun fromEnum(enum: IdentifierType?) = TypeAdapter.fromEnum(enum)
    }

    companion object {
      @VisibleForTesting(otherwise = VisibleForTesting.NONE)
      fun random() = TypeAdapter.knownMappings.keys.shuffled().first()
    }
  }
}
