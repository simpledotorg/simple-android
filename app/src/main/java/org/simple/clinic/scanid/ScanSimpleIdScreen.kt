package org.simple.clinic.scanid

import android.content.Context
import android.graphics.Rect
import android.os.Parcelable
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.widget.editorActionEvents
import com.jakewharton.rxbinding3.widget.textChangeEvents
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_scan_simple.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport.SHORT_CODE_LENGTH
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.ui.ShortCodeSpanWatcher
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.qrcodescanner.IQrCodeScannerView
import org.simple.clinic.widgets.qrcodescanner.QrCodeScannerView
import javax.inject.Inject

class ScanSimpleIdScreen(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs), ScanSimpleIdUiActions {

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var effectHandlerFactory: ScanSimpleIdEffectHandler.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  private val keyboardVisibilityDetector = KeyboardVisibilityDetector()

  private lateinit var qrCodeScannerView: IQrCodeScannerView

  var scanResultsReceiver: ScanResultsReceiver? = null

  private val events by unsafeLazy {
    Observable
        .mergeArray(qrScans(), keyboardEvents(), qrCodeChanges(), doneClicks())
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = ScanSimpleIdModel.create(),
        update = ScanSimpleIdUpdate(),
        effectHandler = effectHandlerFactory.create(this).build()
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)

    // It is possible that going back via the app bar from future screens will come back to this
    // screen with the keyboard open. So, we hide it here.
    hideKeyboard()
    toolBar.setNavigationOnClickListener { activity.finish() }
    setupShortCodeTextField()

    qrCodeScannerView = QrCodeScannerView(context)

    qrCodeScannerViewContainer.addView(qrCodeScannerView as View)
  }

  private fun setupShortCodeTextField() {
    with(shortCodeText) {
      filters = arrayOf(LengthFilter(SHORT_CODE_LENGTH))
      addTextChangedListener(ShortCodeSpanWatcher())
    }
  }

  private fun qrScans(): Observable<UiEvent> {
    val scans = qrCodeScannerView
        .scans()
    return scans.map(::ScanSimpleIdScreenQrCodeScanned)
  }

  private fun qrCodeChanges(): Observable<UiEvent> {
    return shortCodeText
        .textChangeEvents()
        .map { ShortCodeChanged }
  }

  private fun doneClicks(): Observable<UiEvent> {
    return shortCodeText
        .editorActionEvents { it.actionId == EditorInfo.IME_ACTION_SEARCH }
        .map { ShortCodeSearched(ShortCodeInput(shortCodeText.text.toString())) }
  }

  private fun keyboardEvents(): Observable<UiEvent> {
    return Observable.create<UiEvent> { emitter ->
      keyboardVisibilityDetector.registerListener(this) { isVisible ->
        val keyboardEvent = if (isVisible) ShowKeyboard else HideKeyboard
        emitter.onNext(keyboardEvent)
      }

      emitter.setCancellable {
        keyboardVisibilityDetector.unregisterListener(this)
      }
    }
  }

  override fun sendScannedId(scanResult: ScanResult) {
    scanResultsReceiver?.onScanResult(scanResult)
  }

  override fun showShortCodeValidationError(failure: ShortCodeValidationResult) {
    shortCodeErrorText.visibility = View.VISIBLE
    val validationErrorMessage = if (failure == Empty) {
      R.string.scansimpleid_shortcode_error_empty
    } else {
      R.string.scansimpleid_shortcode_error_not_required_length
    }
    shortCodeErrorText.text = resources.getString(validationErrorMessage)
  }

  override fun hideShortCodeValidationError() {
    shortCodeErrorText.visibility = View.GONE
  }

  override fun hideQrCodeScannerView() {
    qrCodeScannerView.hideQrCodeScanner()
  }

  override fun showQrCodeScannerView() {
    qrCodeScannerView.showQrCodeScanner()
  }

  interface Injector {
    fun inject(target: ScanSimpleIdScreen)
  }

  interface ScanResultsReceiver {
    fun onScanResult(scanResult: ScanResult)
  }
}

class KeyboardVisibilityDetector {
  private var isKeyboardShowing = false
  private var layoutListener: OnGlobalLayoutListener? = null

  fun registerListener(view: View, visibilityChangeListener: (Boolean) -> Unit) {
    check(layoutListener == null) {
      "A listener is already registered. You must call `unregisterListener` before calling `registerListener` again."
    }

    layoutListener = OnGlobalLayoutListener {
      val rect = Rect()
      view.getWindowVisibleDisplayFrame(rect)
      val screenHeight = view.rootView.height

      val keypadHeight = screenHeight - rect.bottom

      if (keypadHeight > screenHeight * 0.15) { // 0.15 is a magic number, completely irrational
        if (!isKeyboardShowing) {
          isKeyboardShowing = true
          visibilityChangeListener(true)
        }
      } else {
        if (isKeyboardShowing) {
          isKeyboardShowing = false
          visibilityChangeListener(false)
        }
      }
    }
    view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
  }

  fun unregisterListener(view: View) {
    view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
  }
}
