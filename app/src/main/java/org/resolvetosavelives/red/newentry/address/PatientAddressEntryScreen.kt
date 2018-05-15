package org.resolvetosavelives.red.newentry.address

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.mobile.PatientMobileEntryScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.widgets.showKeyboard
import javax.inject.Inject

class PatientAddressEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  private val streetEditText by bindView<EditText>(R.id.patiententry_address_street)
  private val nextButton by bindView<TextView>(R.id.patientaddress_next_button)

  companion object {
    val KEY = PatientAddressEntryScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    streetEditText.showKeyboard()

    nextButton.setOnClickListener({
      screenRouter.push(PatientMobileEntryScreen.KEY)
    })
  }
}

