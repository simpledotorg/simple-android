package org.simple.clinic.bp.entry

import org.threeten.bp.LocalDate
import java.util.UUID

interface BloodPressureEntryUi {
  fun changeFocusToDiastolic()
  fun changeFocusToSystolic()
  fun setBpSavedResultAndFinish()
  fun hideBpErrorMessage()
  fun showSystolicLessThanDiastolicError()
  fun showSystolicLowError()
  fun showSystolicHighError()
  fun showDiastolicLowError()
  fun showDiastolicHighError()
  fun showSystolicEmptyError()
  fun showDiastolicEmptyError()
  fun setSystolic(systolic: String)
  fun setDiastolic(diastolic: String)
  fun showRemoveBpButton()
  fun hideRemoveBpButton()
  fun showEnterNewBloodPressureTitle()
  fun showEditBloodPressureTitle()
  fun showConfirmRemoveBloodPressureDialog(uuid: UUID)
  fun showBpEntryScreen()
  fun showDateEntryScreen()
  fun showInvalidDateError()
  fun showDateIsInFutureError()
  fun hideDateErrorMessage()
  fun setDateOnInputFields(dayOfMonth: String, month: String, twoDigitYear: String)
  fun showDateOnDateButton(date: LocalDate)
  fun dismiss()
}
