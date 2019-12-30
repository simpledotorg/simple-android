package org.simple.clinic.bloodsugar.entry

import android.os.Bundle
import org.simple.clinic.R
import org.simple.clinic.widgets.BottomSheetActivity
import org.threeten.bp.LocalDate

class BloodSugarEntrySheet : BottomSheetActivity(), BloodSugarEntryUi {

  enum class ScreenType {
    BLOOD_SUGAR_ENTRY,
    DATE_ENTRY
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_blood_sugar_entry)
  }

  override fun setBloodSugarSavedResultAndFinish() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun hideBloodSugarErrorMessage() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showBloodSugarEmptyError() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showBloodSugarHighError() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showBloodSugarLowError() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showRandomBloodSugarTitle() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showPostPrandialBloodSugarTitle() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showFastingBloodSugarTitle() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showBloodSugarEntryScreen() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showDateEntryScreen() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showInvalidDateError() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showDateIsInFutureError() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun hideDateErrorMessage() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun setDateOnInputFields(dayOfMonth: String, month: String, twoDigitYear: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showDateOnDateButton(date: LocalDate) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun dismiss() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
