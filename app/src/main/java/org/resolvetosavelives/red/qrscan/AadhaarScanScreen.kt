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
import org.resolvetosavelives.red.router.screen.ActivityPermissionResult
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.util.RuntimePermissions
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.ScreenDestroyed
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

  fun requestCameraPermission() {
    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), REQUESTCODE_CAMERA_PERMISSION)
  }

  fun qrCodeScans(): Observable<QrScanned> {
    return qrReaderView.scans()
        .map { qrCode -> QrScanned(qrCode) }
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
}
