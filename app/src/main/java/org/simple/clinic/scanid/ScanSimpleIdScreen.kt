package org.simple.clinic.scanid

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreenKey
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.qrcodescanner.QrCodeScannerView
import javax.inject.Inject

class ScanSimpleIdScreen(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: ScanSimpleIdScreenController

  private val qrCodeScannerView by bindView<QrCodeScannerView>(R.id.scansimpleid_code_scanner_view)

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)
    // It is possible that going back via the app bar from future screens will come back to this
    // screen with the keyboard open. So, we hide it here.
    hideKeyboard()

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }

    Observable.merge(screenDestroys, qrScans())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(screenDestroys)
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun qrScans(): Observable<UiEvent> {
    return qrCodeScannerView
        .scans()
        .map(::ScanSimpleIdScreenQrCodeScanned)
  }

  fun showPatientSearchResults(qrCode: String) {
    Toast.makeText(context, "Scanned: $qrCode", Toast.LENGTH_SHORT).show()
    screenRouter.push(PatientSearchScreenKey())
  }
}
