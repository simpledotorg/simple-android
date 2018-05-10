package org.resolvetosavelives.red.newentry.mobile

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import kotterknife.bindView
import org.resolvetosavelives.red.R

class PatientMobileEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientMobileEntryScreenKey()
  }

  private val nextButton: Button by bindView(R.id.patientmobile_next)

  override fun onFinishInflate() {
    super.onFinishInflate()

    nextButton.setOnClickListener({
      // TODO.
    })
  }
}
