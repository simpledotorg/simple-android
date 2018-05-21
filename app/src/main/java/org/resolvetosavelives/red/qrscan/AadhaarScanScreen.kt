package org.resolvetosavelives.red.qrscan

import android.Manifest
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.personal.PatientPersonalDetailsEntryScreen
import org.resolvetosavelives.red.router.screen.ActivityPermissionResult
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.util.RuntimePermissions
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.ScreenDestroyed
import timber.log.Timber
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

    // TODO: Emit on every app resume so that scanner can be enabled
    // TODO: automatically when the user returns from App Info.
    val viewResumes = screenCreates()

    return Observable.merge(viewResumes, permissionGrants)
        .map { RuntimePermissions.check(context, Manifest.permission.CAMERA) }
        .map(::CameraPermissionChanged)
  }

  fun requestCameraPermission() {
    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), REQUESTCODE_CAMERA_PERMISSION)
  }

  fun openAppInfoToManuallyEnableCameraAccess() {
    // TODO.
  }

  fun setupQrScanner() {
    qrReaderView.setup()
  }

  fun qrCodeScans(): Observable<QrScanned> {
    return qrReaderView.scans()
        .map { qrCode -> QrScanned(qrCode) }
  }

  fun setAadhaarScannerEnabled(enabled: Boolean) {
    if (enabled) {
      Timber.w("Enabling aadhaar")
      qrReaderView.start()
    } else {
      Timber.w("Disabling aadhaar")
      qrReaderView.stop()
    }
  }

  fun openNewPatientEntryScreen() {
    screenRouter.push(PatientPersonalDetailsEntryScreen.KEY)
  }
}
