package org.simple.clinic.patient

import android.support.annotation.StringRes
import com.squareup.moshi.Json
import org.simple.clinic.R
import org.simple.clinic.util.RoomEnumTypeConverter

enum class Gender(@StringRes val displayTextRes: Int, val displayLetterRes: Int) {

  @Json(name = "male")
  MALE(R.string.gender_male, R.string.gender_male_letter),

  @Json(name = "female")
  FEMALE(R.string.gender_female, R.string.gender_female_letter),

  @Json(name = "transgender")
  TRANSGENDER(R.string.gender_transgender, R.string.gender_trans_letter);

  class RoomTypeConverter : RoomEnumTypeConverter<Gender>(Gender::class.java)
}
