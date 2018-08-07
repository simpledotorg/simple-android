package org.simple.clinic.qrscan

import android.Manifest
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import javax.inject.Inject

class AadhaarScanScreen(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  companion object {
    const val REQUESTCODE_CAMERA_PERMISSION = 99
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: AadhaarScanScreenController

  @Inject
  lateinit var activity: TheActivity

  private lateinit var qrReaderView: QrCodeScannerView

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    qrReaderView = findViewById<View>(R.id.aadhaarscanner_qr_scanner) as QrCodeScannerView

    Observable
        .mergeArray(screenCreates(), screenDestroys(), aadhaarScanClicks(), cameraPermissionChanges(), qrCodeScans())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun aadhaarScanClicks() = RxView.clicks(this)
      .map { AadhaarScanClicked() }

  private fun screenCreates() = RxView.attaches(this)
      .map { ScreenCreated() }

  private fun screenDestroys() = RxView.detaches(this)
      .map { ScreenDestroyed() }

  private fun cameraPermissionChanges(): Observable<CameraPermissionChanged> {
    val permissionGrants = screenRouter.streamScreenResults()
        .ofType<ActivityPermissionResult>()
        .filter { result -> result.requestCode == REQUESTCODE_CAMERA_PERMISSION }

    return Observable.merge(screenCreates(), permissionGrants)
        .map { RuntimePermissions.check(activity, Manifest.permission.CAMERA) }
        .map(::CameraPermissionChanged)
  }

  private fun qrCodeScans(): Observable<QrScanned> {
    return qrReaderView.scans()
        .map { qrCode -> QrScanned(qrCode) }
  }

  fun requestCameraPermission() {
    RuntimePermissions.request(activity, Manifest.permission.CAMERA, REQUESTCODE_CAMERA_PERMISSION)
  }

  fun setAadhaarScannerEnabled(enabled: Boolean) {
    if (enabled) {
      qrReaderView.start()
    } else {
      qrReaderView.stop()
    }
  }

  fun openNewPatientEntryScreen() {
    // TODO.
  }

  fun openPatientSearchScreen(preFilledSearchQuery: String, preFilledAge: String?) {
    // TODO
  }
}
