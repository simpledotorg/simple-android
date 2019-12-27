package org.simple.clinic.bloodsugar.entry

class BloodSugarEntryModel {

  companion object {
    val BLANK = BloodSugarEntryModel()
  }

  fun bloodSugarChanged(): BloodSugarEntryModel = BLANK
  fun dayChanged(): BloodSugarEntryModel = BLANK
  fun monthChanged(): BloodSugarEntryModel = BLANK
  fun yearChanged(): BloodSugarEntryModel = BLANK
}
