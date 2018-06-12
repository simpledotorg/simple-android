package org.resolvetosavelives.red.summary

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.patient.Gender
import org.resolvetosavelives.red.patient.Patient
import org.resolvetosavelives.red.patient.PatientAddress
import org.resolvetosavelives.red.patient.PatientPhoneNumber
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

class PatientSummaryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSummaryScreenController

  private val fullNameTextView by bindView<TextView>(R.id.patientsummary_fullname)
  private val byline1TextView by bindView<TextView>(R.id.patientsummary_byline1)
  private val byline2TextView by bindView<TextView>(R.id.patientsummary_byline2)

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PatientSummaryScreenKey>(this)!!
    return Observable.just(PatientSummaryScreenCreated(screenKey.patientUuid))
  }

  @SuppressLint("SetTextI18n")
  fun preFill(patient: Patient, address: PatientAddress, phoneNumber: PatientPhoneNumber) {
    fullNameTextView.text = patient.fullName
    byline1TextView.text = "${resources.getString(Gender.MALE.displayTextRes)} • ${phoneNumber.number}"
    byline2TextView.text = when {
      address.colonyOrVillage.isNullOrBlank() -> "${address.district}, ${address.state}"
      else -> "${address.colonyOrVillage}, ${address.district}, ${address.state}"
    }
  }

  fun openBloodPressureEntrySheet() {
    // TODO.
  }
}
