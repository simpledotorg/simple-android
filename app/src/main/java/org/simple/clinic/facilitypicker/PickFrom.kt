package org.simple.clinic.facilitypicker

import android.content.res.TypedArray
import org.simple.clinic.R

enum class PickFrom {
  AllFacilities,
  InCurrentGroup;

  companion object {
    fun forAttribute(typedArray: TypedArray): PickFrom {
      return when (val enumValue = typedArray.getInt(R.styleable.FacilityPickerView_pickFrom, -1)) {
        0 -> AllFacilities
        1 -> InCurrentGroup
        else -> throw IllegalStateException("Unknown enum value for `FacilityPickerView#pickFrom`: [$enumValue]")
      }
    }
  }
}
