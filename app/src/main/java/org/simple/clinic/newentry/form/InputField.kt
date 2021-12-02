package org.simple.clinic.newentry.form

import androidx.annotation.StringRes

sealed class InputField<T>(@StringRes val labelResId: Int) {
  abstract fun validate(value: T): Set<ValidationError>
}
