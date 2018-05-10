package org.resolvetosavelives.red.newentry.mobile

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity

class PatientMobileEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientMobileEntryScreenKey()
  }

  private val primaryMobileNumberEditText: EditText by bindView(R.id.patientmobile_primary_number)
  private val nextButton: Button by bindView(R.id.patientmobile_next)

  override fun onFinishInflate() {
    super.onFinishInflate()

    // TODO: threading.
    TheActivity.patientRepository()
        .ongoingEntry()
        .subscribe({ entry ->
          primaryMobileNumberEditText.setText(entry.mobileNumber)
        })

    nextButton.setOnClickListener({
      // TODO.
    })
  }
}

