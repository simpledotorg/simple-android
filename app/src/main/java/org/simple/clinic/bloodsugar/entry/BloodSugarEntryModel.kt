package org.simple.clinic.bloodsugar.entry

class BloodSugarEntryModel {

  companion object {
    val BLANK = BloodSugarEntryModel()
  }

  fun bloodSugarChanged(): BloodSugarEntryModel = BLANK
}
