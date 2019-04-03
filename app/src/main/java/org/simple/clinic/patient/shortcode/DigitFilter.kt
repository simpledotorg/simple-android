package org.simple.clinic.patient.shortcode

class DigitFilter: UuidShortCodeCreator.CharacterFilter {

  override fun filter(char: Char): Boolean {
    return char.isDigit()
  }
}
