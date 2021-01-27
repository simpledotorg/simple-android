package org.simple.clinic.scanid

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding3.widget.editorActionEvents
import com.jakewharton.rxbinding3.widget.textChangeEvents
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenScanSimpleBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport.SHORT_CODE_LENGTH
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.ui.ShortCodeSpanWatcher
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.qrcodescanner.IQrCodeScannerView
import org.simple.clinic.widgets.qrcodescanner.QrCodeScannerView
import javax.inject.Inject

class ScanSimpleIdScreen :
    BaseScreen<
        ScanSimpleIdScreenKey,
        ScreenScanSimpleBinding,
        ScanSimpleIdModel,
        ScanSimpleIdEvent,
        ScanSimpleIdEffect>(),
    ScanSimpleIdUi,
    ScanSimpleIdUiActions {

  companion object {
    fun readScanResult(result: Succeeded): ScanResult {
      return result.result as ScanResult
    }
  }

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var effectHandlerFactory: ScanSimpleIdEffectHandler.Factory

  @Inject
  lateinit var scanSimpleIdUpdate: ScanSimpleIdUpdate

  @Inject
  lateinit var router: Router

  private val toolBar
    get() = binding.toolBar

  private val qrCodeScannerViewContainer
    get() = binding.qrCodeScannerViewContainer

  private val shortCodeText
    get() = binding.shortCodeText

  private val shortCodeErrorText
    get() = binding.shortCodeErrorText

  private val searchingContainer
    get() = binding.searchingContainer

  private val keyboardVisibilityDetector = KeyboardVisibilityDetector()

  private lateinit var qrCodeScannerView: IQrCodeScannerView

  override fun defaultModel() = ScanSimpleIdModel.create()

  override fun uiRenderer() = ScanSimpleIdUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenScanSimpleBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .mergeArray(qrScans(), keyboardEvents(), qrCodeChanges(), doneClicks())
      .compose(ReportAnalyticsEvents())
      .cast<ScanSimpleIdEvent>()

  override fun createUpdate() = scanSimpleIdUpdate

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    // This needs to be instantiated here because the `onViewCreated` call of the super class
    // will indirectly reference this instance via the `events()` method.
    qrCodeScannerView = QrCodeScannerView(requireContext())

    super.onViewCreated(view, savedInstanceState)

    // It is possible that going back via the app bar from future screens will come back to this
    // screen with the keyboard open. So, we hide it here.
    binding.root.hideKeyboard()
    toolBar.setNavigationOnClickListener { router.pop() }
    setupShortCodeTextField()

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
        .take(1)
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
      keyboardVisibilityDetector.registerListener(requireView()) { isVisible ->
        val keyboardEvent = if (isVisible) ShowKeyboard else HideKeyboard
        emitter.onNext(keyboardEvent)
      }

      emitter.setCancellable {
        keyboardVisibilityDetector.unregisterListener(requireView())
      }
    }
  }

  override fun sendScannedId(scanResult: ScanResult) {
    router.popWithResult(Succeeded(scanResult))
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

  override fun showSearchingForPatient() {
    searchingContainer.visibility = View.VISIBLE
  }

  override fun hideSearchingForPatient() {
    searchingContainer.visibility = View.GONE
  }

  interface Injector {
    fun inject(target: ScanSimpleIdScreen)
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
