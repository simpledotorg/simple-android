package org.simple.clinic.patient

import android.support.annotation.StringRes
import com.squareup.moshi.Json
import org.simple.clinic.R
import org.simple.clinic.util.RoomEnumTypeConverter

enum class Gender(@StringRes val displayTextRes: Int) {

  @Json(name = "male")
  MALE(R.string.gender_male),

  @Json(name = "female")
  FEMALE(R.string.gender_female),

  @Json(name = "transgender")
  TRANSGENDER(R.string.gender_transgender);

  class RoomTypeConverter : RoomEnumTypeConverter<Gender>(Gender::class.java)
}
