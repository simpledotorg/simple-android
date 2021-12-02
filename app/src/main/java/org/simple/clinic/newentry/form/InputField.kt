package org.simple.clinic.newentry.form

import android.os.Parcelable
import androidx.annotation.StringRes

sealed class InputField<T>(@StringRes val labelResId: Int) : Parcelable {
  abstract fun validate(value: T): Set<ValidationError>
}
