package org.resolvetosavelives.red.newentry.address

import android.content.Context
import android.util.AttributeSet
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
import org.resolvetosavelives.red.newentry.phone.PatientPhoneEntryScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.search.OngoingPatientEntry
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.setTextAndCursor
import org.resolvetosavelives.red.widgets.showKeyboard
import javax.inject.Inject

class PatientAddressEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  private val colonyOrVillageEditText by bindView<EditText>(R.id.patiententry_address_colony_or_village)
  private val districtEditText by bindView<EditText>(R.id.patiententry_address_district)
  private val stateEditText by bindView<EditText>(R.id.patiententry_address_state)
  private val proceedButton by bindView<TextView>(R.id.patiententry_address_proceed)

  companion object {
    val KEY = PatientAddressEntryScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientAddressEntryScreenController

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable
        .mergeArray(screenCreates(), colonyOrVillageTextChanges(), districtTextChanges(), stateTextChanges(), proceedClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = RxView.attaches(this)
      .map { ScreenCreated() }

  private fun colonyOrVillageTextChanges() = RxTextView.textChanges(colonyOrVillageEditText)
      .map(CharSequence::toString)
      .map(::PatientAddressColonyOrVillageTextChanged)

  private fun districtTextChanges() = RxTextView.textChanges(districtEditText)
      .map(CharSequence::toString)
      .map(::PatientAddressDistrictTextChanged)

  private fun stateTextChanges() = RxTextView.textChanges(stateEditText)
      .map(CharSequence::toString)
      .map(::PatientAddressStateTextChanged)

  private fun proceedClicks() = RxView.clicks(proceedButton)
      .map { PatientAddressEntryProceedClicked() }

  fun showKeyboardOnColonyField() {
    colonyOrVillageEditText.showKeyboard()
  }

  fun preFill(address: OngoingPatientEntry.Address) {
    colonyOrVillageEditText.setTextAndCursor(address.colonyOrVillage)
    districtEditText.setTextAndCursor(address.district)
    stateEditText.setTextAndCursor(address.state)
  }

  fun openPatientPhoneEntryScreen() {
    screenRouter.push(PatientPhoneEntryScreen.KEY)
  }
}

