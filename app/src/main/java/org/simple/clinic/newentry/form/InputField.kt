package org.simple.clinic.newentry.form

import androidx.annotation.StringRes

abstract class InputField<T>(@StringRes val labelResId: Int) {
  abstract fun validate(value: T): Set<ValidationError>
}
