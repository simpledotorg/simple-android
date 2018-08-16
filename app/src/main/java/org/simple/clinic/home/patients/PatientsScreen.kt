package org.simple.clinic.home.patients

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
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreen
import org.simple.clinic.widgets.TheActivityLifecycle
import javax.inject.Inject

open class PatientsScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientsScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientsScreenController

  @Inject
  lateinit var activityLifecycle: Observable<TheActivityLifecycle>

  private val searchButton by bindView<Button>(R.id.patients_search_patients)
  private val aadhaarScanButton by bindView<Button>(R.id.patients_scan_aadhaar)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    Observable.merge(activityLifecycle, aadhaarScanButtonClicks(), searchButtonClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun aadhaarScanButtonClicks() = RxView.clicks(aadhaarScanButton).map { ScanAadhaarClicked() }

  private fun searchButtonClicks() = RxView.clicks(searchButton).map { NewPatientClicked() }

  fun openNewPatientScreen() {
    screenRouter.push(PatientSearchScreen.KEY)
  }
}
