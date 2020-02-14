package org.simple.clinic.bloodsugar.entry

import org.threeten.bp.LocalDate

interface BloodSugarEntryUi {
  fun setBloodSugarSavedResultAndFinish()
  fun hideBloodSugarErrorMessage()
  fun showBloodSugarEmptyError()
  fun showBloodSugarHighError()
  fun showBloodSugarLowError()
  fun showRandomBloodSugarTitle()
  fun showPostPrandialBloodSugarTitle()
  fun showFastingBloodSugarTitle()
  fun showBloodSugarEntryScreen()
  fun showDateEntryScreen()
  fun showInvalidDateError()
  fun showDateIsInFutureError()
  fun hideDateErrorMessage()
  fun setDateOnInputFields(dayOfMonth: String, month: String, twoDigitYear: String)
  fun showDateOnDateButton(date: LocalDate)
  fun showEditRadomBloodSugarTitle()
  fun showEditPostPrandialBloodSugarTitle()
  fun showEditFastingBloodSugarTitle()
  fun showRemoveButton()
  fun hideRemoveButton()
  fun dismiss()
}
