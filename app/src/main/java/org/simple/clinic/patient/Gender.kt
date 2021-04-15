package org.simple.clinic.patient

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
import org.simple.clinic.patient.Gender.Unknown
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class Gender: Parcelable {

  @Parcelize
  object Male : Gender()

  @Parcelize
  object Female : Gender()

  @Parcelize
  object Transgender : Gender()

  @Parcelize
  data class Unknown(val actualValue: String): Gender()

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  object TypeAdapter: SafeEnumTypeAdapter<Gender>(
      knownMappings = mapOf(
          Male to "male",
          Female to "female",
          Transgender to "transgender"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {

    @TypeConverter
    fun toEnum(value: String?): Gender? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(answer: Gender): String? = TypeAdapter.fromEnum(answer)
  }

  class MoshiTypeAdapter {

    @FromJson
    fun fromJson(value: String?): Gender? = TypeAdapter.toEnum(value)

    @ToJson
    fun toJson(answer: Gender): String? = TypeAdapter.fromEnum(answer)
  }
}

val Gender.displayLetterRes: Int
    get() = when(this) {
      Male -> R.string.gender_male_letter
      Female -> R.string.gender_female_letter
      Transgender -> R.string.gender_trans_letter
      is Unknown -> R.string.gender_unknown_letter
    }

val Gender.displayIconRes: Int
  get() = when(this) {
    Male -> R.drawable.ic_patient_male
    Female -> R.drawable.ic_patient_female
    Transgender -> R.drawable.ic_patient_transgender
    is Unknown -> R.drawable.ic_patient_unknown
  }
