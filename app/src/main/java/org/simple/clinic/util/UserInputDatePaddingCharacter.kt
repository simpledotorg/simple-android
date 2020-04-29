package org.simple.clinic.util

// TODO: Convert to inline class once they become stable.
data class UserInputDatePaddingCharacter(val value: Char) {
  companion object {
    val ZERO = UserInputDatePaddingCharacter('0')
  }
}
