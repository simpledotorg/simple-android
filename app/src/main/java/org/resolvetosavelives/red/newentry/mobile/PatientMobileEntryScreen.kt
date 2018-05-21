package org.resolvetosavelives.red.newentry.mobile

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.bp.PatientBpEntryScreen
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.setTextAndCursor
import org.resolvetosavelives.red.widgets.showKeyboard
import javax.inject.Inject

class PatientMobileEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientMobileEntryScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientMobileEntryScreenController

  private val primaryNumberEditText by bindView<EditText>(R.id.patiententry_mobile_primary)
  private val secondaryNumberEditText by bindView<EditText>(R.id.patiententry_mobile_secondary)
  private val proceedButton by bindView<Button>(R.id.patiententry_mobile_proceed)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable
        .merge(screenCreates(), proceedClicks())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    showKeyboardOnPrimaryMobileNumber()

    //    patientRepository
    //        .ongoingEntry()
    //        .subscribeOn(io())
    //        .observeOn(mainThread())
    //        .subscribe({ entry ->
    //          preFill(entry)
    //        })
  }

  private fun screenCreates() = RxView.attaches(this)
      .map { ScreenCreated() }

  private fun proceedClicks() = RxView.clicks(proceedButton)
      .map { PatientMobileEntryProceedClicked() }

  fun showKeyboardOnPrimaryMobileNumber() {
    primaryNumberEditText.showKeyboard()
  }

  fun preFill(numbers: OngoingPatientEntry.MobileNumbers) {
    primaryNumberEditText.setTextAndCursor(numbers.primary)
    secondaryNumberEditText.setTextAndCursor(numbers.secondary ?: "")
  }

  fun openBloodPressureEntryScreen() {
    screenRouter.push(PatientBpEntryScreen.KEY)
  }
}

