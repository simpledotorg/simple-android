package org.simple.clinic.scanid

data class ShortCodeInput(val shortCodeText: String) {
  fun isValid(): Boolean {
    return shortCodeText.length == 7
  }
}
