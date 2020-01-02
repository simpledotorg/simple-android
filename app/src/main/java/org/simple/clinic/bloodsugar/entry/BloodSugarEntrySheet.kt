package org.simple.clinic.bloodsugar.entry

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.android.synthetic.main.sheet_blood_sugar_entry.*
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class BloodSugarEntrySheet : BottomSheetActivity(), BloodSugarEntryUi {
  enum class ScreenType {
    BLOOD_SUGAR_ENTRY,
    DATE_ENTRY
  }

  companion object {
    private const val EXTRA_WAS_BLOOD_SUGAR_SAVED = "wasBloodSugarSaved"
  }

  @Inject
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var userInputDatePaddingCharacter: UserInputDatePaddingCharacter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_blood_sugar_entry)
    TheActivity.component.inject(this)
  }

  override fun setBloodSugarSavedResultAndFinish() {
    val intent = Intent()
    intent.putExtra(EXTRA_WAS_BLOOD_SUGAR_SAVED, true)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  override fun hideBloodSugarErrorMessage() {
    bloodSugarErrorTextView.visibleOrGone(false)
  }

  override fun showBloodSugarEmptyError() {
    bloodSugarErrorTextView.text = getString(R.string.bloodsugarentry_error_empty)
  }

  override fun showBloodSugarHighError() {
    bloodSugarErrorTextView.text = getString(R.string.bloodsugarentry_error_higher_limit)
  }

  override fun showBloodSugarLowError() {
    bloodSugarErrorTextView.text = getString(R.string.bloodsugarentry_error_lower_limit)
  }

  override fun showRandomBloodSugarTitle() {
    enterBloodSugarTitleTextView.text = getString(R.string.bloodsugarentry_random_title)
  }

  override fun showPostPrandialBloodSugarTitle() {
    enterBloodSugarTitleTextView.text = getString(R.string.bloodsugarentry_post_prandial_title)
  }

  override fun showFastingBloodSugarTitle() {
    enterBloodSugarTitleTextView.text = getString(R.string.bloodsugarentry_fasting_title)
  }

  override fun showBloodSugarEntryScreen() {
    viewFlipper.inAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_reading_entry_from_left)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.outAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_date_exit_to_right)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.displayedChildResId = R.id.bloodsugarentry_flipper_blood_sugar_entry
  }

  override fun showDateEntryScreen() {
    viewFlipper.inAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_date_entry_from_right)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.outAnimation = AnimationUtils
        .loadAnimation(this, R.anim.measurementinput_reading_exit_to_left)
        .apply { interpolator = FastOutSlowInInterpolator() }

    viewFlipper.displayedChildResId = R.id.bloodsugarentry_flipper_date_entry
    yearEditText.requestFocus()
  }

  override fun showInvalidDateError() {
    showDateErrorMessage(getString(R.string.bloodsugarentry_error_date_invalid_pattern))
  }

  override fun showDateIsInFutureError() {
    showDateErrorMessage(getString(R.string.bloodsugarentry_error_date_is_in_future))
  }

  override fun hideDateErrorMessage() {
    dateErrorTextView.visibleOrGone(false)
  }

  override fun setDateOnInputFields(dayOfMonth: String, month: String, twoDigitYear: String) {
    dayEditText.setTextAndCursor(getPaddedString(dayOfMonth))
    monthEditText.setTextAndCursor(getPaddedString(month))
    yearEditText.setTextAndCursor(twoDigitYear)
  }

  override fun showDateOnDateButton(date: LocalDate) {
    bloodSugarDateButton.text = dateFormatter.format(date)
  }

  override fun dismiss() {
    finish()
  }

  private fun showDateErrorMessage(message: String) {
    with(dateErrorTextView) {
      text = message
      visibility = View.VISIBLE
    }
  }

  private fun getPaddedString(value: String): String =
      value.padStart(length = 2, padChar = userInputDatePaddingCharacter.value)
}
