package org.resolvetosavelives.red.newentry.bp

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.drugs.PatientCurrentDrugsEntryScreen
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.setTextAndCursor
import org.resolvetosavelives.red.widgets.showKeyboard
import javax.inject.Inject

class PatientBpEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientBpEntryScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientBpEntryScreenController

  private val patientFullNameTextView by bindView<TextView>(R.id.patiententry_bp_patient_fullname)
  private val systolicEditText by bindView<EditText>(R.id.patiententry_bp_systolic)
  private val diastolicEditText by bindView<EditText>(R.id.patiententry_bp_diastolic)
  private val proceedButton by bindView<Button>(R.id.patiententry_bp_proceed)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.merge(screenCreates(), systolicTextChanges(), diastolicTextChanges(), proceedClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = RxView.attaches(this)
      .map { ScreenCreated() }

  private fun systolicTextChanges() = RxTextView.textChanges(systolicEditText)
      .map { measurementText -> measurementText.toString().toInt() }
      .map { measurement -> PatientBpSystolicTextChanged(measurement) }

  private fun diastolicTextChanges() = RxTextView.textChanges(diastolicEditText)
      .map { measurementText -> measurementText.toString().toInt() }
      .map { measurement -> PatientBpDiastolicTextChanged(measurement) }

  private fun proceedClicks() = RxView.clicks(proceedButton)
      .map { PatientBpEntryProceedClicked() }

  fun showKeyboardOnSystolicField() {
    systolicEditText.showKeyboard()
  }

  fun preFill(patientFullName: String) {
    patientFullNameTextView.text = patientFullName
  }

  fun preFill(measurements: OngoingPatientEntry.BloodPressureMeasurement) {
    systolicEditText.setTextAndCursor(measurements.systolic.toString())
    diastolicEditText.setTextAndCursor(measurements.diastolic.toString())
  }

  fun openDrugSelectionScreen() {
    screenRouter.push(PatientCurrentDrugsEntryScreen.KEY)
  }
}
