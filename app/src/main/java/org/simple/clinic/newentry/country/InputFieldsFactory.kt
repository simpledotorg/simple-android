package org.simple.clinic.newentry.country

import org.simple.clinic.newentry.form.InputField
import javax.inject.Inject

class InputFieldsFactory @Inject constructor(
    private val inputFieldsProvider: InputFieldsProvider
) {

  fun provideFields(): List<InputField<*>> {
    return inputFieldsProvider.provide()
  }
}
