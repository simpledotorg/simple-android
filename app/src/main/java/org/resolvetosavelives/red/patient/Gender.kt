package org.resolvetosavelives.red.patient

import android.support.annotation.StringRes
import com.squareup.moshi.Json
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.util.RoomEnumTypeConverter

enum class Gender(@StringRes val displayTextRes: Int) {

  @Json(name = "male")
  MALE(R.string.gender_male),

  @Json(name = "female")
  FEMALE(R.string.gender_female),

  @Json(name = "transgender")
  TRANSGENDER(R.string.gender_transgender);

  class RoomTypeConverter : RoomEnumTypeConverter<Gender>(Gender::class.java)
}
