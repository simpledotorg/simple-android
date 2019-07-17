package org.simple.clinic.patient

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.squareup.moshi.Json
import org.simple.clinic.R
import org.simple.clinic.util.RoomEnumTypeConverter

enum class Gender(
    @StringRes val displayLetterRes: Int,
    @DrawableRes val displayIconRes: Int
) {

  @Json(name = "male")
  MALE(R.string.gender_male_letter, R.drawable.ic_patient_male),

  @Json(name = "female")
  FEMALE(R.string.gender_female_letter, R.drawable.ic_patient_female),

  @Json(name = "transgender")
  TRANSGENDER(R.string.gender_trans_letter, R.drawable.ic_patient_transgender);

  class RoomTypeConverter : RoomEnumTypeConverter<Gender>(Gender::class.java)
}
