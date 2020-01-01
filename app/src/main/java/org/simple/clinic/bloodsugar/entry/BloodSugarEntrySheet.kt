package org.simple.clinic.bloodsugar.entry

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.android.synthetic.main.sheet_blood_sugar_entry.*
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class BloodSugarEntrySheet : BottomSheetActivity(), BloodSugarEntryUi {
  @Inject
  lateinit var dateFormatter: DateTimeFormatter

  enum class ScreenType {
    BLOOD_SUGAR_ENTRY,
    DATE_ENTRY
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_blood_sugar_entry)
    TheActivity.component.inject(this)
  }

  override fun setBloodSugarSavedResultAndFinish() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun hideBloodSugarErrorMessage() {
    bsErrorTextView.visibleOrGone(false)
  }

  override fun showBloodSugarEmptyError() {
    bsErrorTextView.text = getString(R.string.bloodsugarentry_error_empty)
  }

  override fun showBloodSugarHighError() {
    bsErrorTextView.text = getString(R.string.bloodsugarentry_error_higher_limit)
  }

  override fun showBloodSugarLowError() {
    bsErrorTextView.text = getString(R.string.bloodsugarentry_error_lower_limit)
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

    viewFlipper.displayedChildResId = R.id.bloodsugarentry_flipper_bs_entry
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
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showDateIsInFutureError() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun hideDateErrorMessage() {
    dateErrorTextView.visibleOrGone(false)
  }

  override fun setDateOnInputFields(dayOfMonth: String, month: String, twoDigitYear: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showDateOnDateButton(date: LocalDate) {
    bsDateButton.text = dateFormatter.format(date)
  }

  override fun dismiss() {
    finish()
  }
}
