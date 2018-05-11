package org.resolvetosavelives.red.newentry.personal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.address.PatientAddressEntryScreen
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry

class PatientPersonalDetailsEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientPersonalDetailsScreenKey()
  }

  private val fullNameEditText by bindView<EditText>(R.id.patientpersonaldetails_full_name)
  private val nextButton by bindView<View>(R.id.patientpersonaldetails_next_button)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    nextButton.setOnClickListener({
      val repository = TheActivity.patientRepository()

      // TODO: threading.
      repository.ongoingEntry()
          .map { entry -> entry.copy(personalDetails = OngoingPatientEntry.PersonalDetails(fullNameEditText.text.toString())) }
          .flatMapCompletable { entry: OngoingPatientEntry -> repository.save(entry) }
          .subscribe({
            TheActivity.screenRouter().push(PatientAddressEntryScreen.KEY)
          })
    })
  }
}
