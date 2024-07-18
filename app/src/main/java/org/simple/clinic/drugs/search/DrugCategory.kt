package org.simple.clinic.drugs.search

import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class DrugCategory {

  sealed class Hypertension : DrugCategory() {

    data object CCB : Hypertension()

    data object ARB : Hypertension()

    data object ACE : Hypertension()

    data object Diuretic : Hypertension()

    data object Other : Hypertension()
  }

  data object Diabetes : DrugCategory()

  data object Other : DrugCategory()

  data class Unknown(val actualValue: String) : DrugCategory()

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  object TypeAdapter : SafeEnumTypeAdapter<DrugCategory>(
      knownMappings = mapOf(
          Hypertension.CCB to "hypertension_ccb",
          Hypertension.ARB to "hypertension_arb",
          Hypertension.ACE to "hypertension_ace",
          Hypertension.Diuretic to "hypertension_diuretic",
          Hypertension.Other to "hypertension_other",
          Diabetes to "diabetes",
          Other to "other"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {
    @TypeConverter
    fun toEnum(value: String?): DrugCategory? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(drugCategory: DrugCategory?): String? = TypeAdapter.fromEnum(drugCategory)
  }

  class MoshiTypeConverter {
    @FromJson
    fun toEnum(value: String?): DrugCategory? = TypeAdapter.toEnum(value)

    @ToJson
    fun fromEnum(drugCategory: DrugCategory?): String? = TypeAdapter.fromEnum(drugCategory)
  }
}
