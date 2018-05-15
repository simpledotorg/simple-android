package org.resolvetosavelives.red.newentry.bp

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.drugs.PatientCurrentDrugsEntryScreen
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.widgets.showKeyboard
import javax.inject.Inject

class PatientBpEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = BpEntryScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var patientRepository: PatientRepository

  private val patientNameTextView by bindView<TextView>(R.id.patiententry_bp_patient_fullname)
  private val systolicEditText by bindView<EditText>(R.id.patiententry_bp_systolic)
  private val diastolicEditText by bindView<EditText>(R.id.patiententry_bp_diastolic)
  private val proceedButton by bindView<Button>(R.id.patiententry_bp_proceed)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    patientRepository
        .ongoingEntry()
        .map { entry ->
          when {
            entry.personalDetails == null -> "(no name)"
            else -> entry.personalDetails.fullName
          }
        }
        .subscribeOn(io())
        .observeOn(mainThread())
        .subscribe({ patientFullName -> patientNameTextView.text = patientFullName })

    systolicEditText.showKeyboard()

    systolicEditText.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(text: Editable?) {
        if (text != null && text.length == 3) {
          diastolicEditText.requestFocus()
        }
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })

    proceedButton.setOnClickListener({
      screenRouter.push(PatientCurrentDrugsEntryScreen.KEY)
    })
  }
}
