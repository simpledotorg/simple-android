package org.simple.clinic.newentry.country

import org.simple.clinic.newentry.form.InputField

interface InputFieldsProvider {
  fun provide(): List<InputField<*>>
}
