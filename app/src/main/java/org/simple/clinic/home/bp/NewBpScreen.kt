package org.simple.clinic.home.bp

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.TheActivity
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreen
import javax.inject.Inject

open class NewBpScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = NewBpScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: NewBpScreenController

  @Inject
  lateinit var patientRepository: PatientRepository

  private val searchButton by bindView<Button>(R.id.home_search_patients)
  private val aadhaarScanButton by bindView<Button>(R.id.home_scan_aadhaar)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    Observable.merge(aadhaarScanButtonClicks(), searchButtonClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun aadhaarScanButtonClicks() = RxView.clicks(aadhaarScanButton)
      .map { ScanAadhaarClicked() }

  private fun searchButtonClicks() = RxView.clicks(searchButton)
      .map { NewPatientClicked() }

  fun openAadhaarScanScreen() {
    // TODO
  }

  fun openNewPatientScreen() {
    screenRouter.push(PatientSearchScreen.KEY)
  }
}
