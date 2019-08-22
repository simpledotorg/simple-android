package org.simple.clinic.scanid

import org.simple.clinic.SHORT_CODE_REQUIRED_LENGTH

data class ShortCodeInput(val shortCodeText: String) {
  fun isValid(): Boolean {
    return shortCodeText.length == SHORT_CODE_REQUIRED_LENGTH
  }
}
