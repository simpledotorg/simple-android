package org.simple.clinic.scanid

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.jakewharton.rxbinding3.widget.editorActionEvents
import com.jakewharton.rxbinding3.widget.textChangeEvents
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenScanSimpleBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport.SHORT_CODE_LENGTH
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.ui.ShortCodeSpanWatcher
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.util.BitmapUtils
import org.simple.clinic.scanid.qrcodeanalyzer.MLKitQrCodeAnalyzer
import org.simple.clinic.scanid.qrcodeanalyzer.ZxingQrCodeAnalyzer
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ScanSimpleIdScreen : BaseScreen<
    ScanSimpleIdScreenKey,
    ScreenScanSimpleBinding,
    ScanSimpleIdModel,
    ScanSimpleIdEvent,
    ScanSimpleIdEffect>(), ScanSimpleIdUi, ScanSimpleIdUiActions {

  companion object {
    private const val RATIO_4_3_VALUE = 4.0 / 3.0
    private const val RATIO_16_9_VALUE = 16.0 / 9.0

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

  @Inject
  lateinit var bitmapUtils: BitmapUtils

  @Inject
  lateinit var googleApiAvailability: GoogleApiAvailability

  private val toolBar
    get() = binding.toolBar

  private val shortCodeText
    get() = binding.shortCodeText

  private val shortCodeErrorText
    get() = binding.shortCodeErrorText

  private val searchingContainer
    get() = binding.searchingContainer

  private val previewView
    get() = binding.previewView

  private val viewFinderImageView
    get() = binding.viewFinderImageView

  private val keyboardVisibilityDetector = KeyboardVisibilityDetector()
  private val cameraExecutor = Executors.newSingleThreadExecutor()
  private val cameraProviderFuture by unsafeLazy {
    ProcessCameraProvider.getInstance(requireContext())
  }

  private val qrScans = PublishSubject.create<ScanSimpleIdEvent>()

  override fun defaultModel() = ScanSimpleIdModel.create()

  override fun uiRenderer() = ScanSimpleIdUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenScanSimpleBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .mergeArray(qrScans, keyboardEvents(), qrCodeChanges(), doneClicks())
      .compose(ReportAnalyticsEvents())
      .cast<ScanSimpleIdEvent>()

  override fun createUpdate() = scanSimpleIdUpdate

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // It is possible that going back via the app bar from future screens will come back to this
    // screen with the keyboard open. So, we hide it here.
    binding.root.hideKeyboard()
    toolBar.setNavigationOnClickListener { router.pop() }
    setupShortCodeTextField()

    cameraProviderFuture.addListener({
      val cameraProvider = cameraProviderFuture.get()
      startCamera(cameraProvider)
    }, ContextCompat.getMainExecutor(requireContext()))
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun startCamera(cameraProvider: ProcessCameraProvider) {
    // Get screen metrics used to setup camera for full screen resolution
    val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }

    val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

    val rotation = previewView.display.rotation

    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    val preview = Preview.Builder().build()

    val analyzer = ImageAnalysis.Builder()
        .setTargetAspectRatio(screenAspectRatio)
        .setTargetRotation(rotation)
        .build()

    val googlePlayServicesAvailability = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
    val isGooglePlayServicesAvailable = googlePlayServicesAvailability == ConnectionResult.SUCCESS

    val isMLKitQrCodeScannerEnabled = features.isEnabled(Feature.MLKitQrCodeScanner)
    val qrCodeAnalyzer = if (isMLKitQrCodeScannerEnabled && isGooglePlayServicesAvailable) {
      MLKitQrCodeAnalyzer(bitmapUtils, ::qrCodeScanned, mlKitUnavailable = {
        setQrCodeAnalyzer(analyzer, ZxingQrCodeAnalyzer(::qrCodeScanned))
      })
    } else {
      ZxingQrCodeAnalyzer(::qrCodeScanned)
    }

    setQrCodeAnalyzer(analyzer, qrCodeAnalyzer)

    cameraProvider.unbindAll()

    val camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, analyzer)
    preview.setSurfaceProvider(previewView.surfaceProvider)

    previewView.setOnTouchListener { _, motionEvent ->
      when (motionEvent.action) {
        MotionEvent.ACTION_DOWN -> true
        MotionEvent.ACTION_UP -> {
          processTapToFocus(motionEvent, camera.cameraControl)
          true
        }
        else -> false
      }
    }
  }

  private fun setQrCodeAnalyzer(analyzer: ImageAnalysis, qrCodeAnalyzer: ImageAnalysis.Analyzer) {
    analyzer.clearAnalyzer()
    analyzer.setAnalyzer(cameraExecutor, qrCodeAnalyzer)
  }

  private fun processTapToFocus(motionEvent: MotionEvent, cameraControl: CameraControl) {
    val meteringPointFactory = previewView.meteringPointFactory
    val point = meteringPointFactory.createPoint(motionEvent.x, motionEvent.y)
    val action = FocusMeteringAction.Builder(point).build()

    cameraControl.startFocusAndMetering(action)
  }

  /**
   *  [androidx.camera.core.ImageAnalysis.Builder.setTargetAspectRatio] requires enum value of
   *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
   *
   *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
   *  of preview ratio to one of the provided values.
   *
   *  @param width - preview width
   *  @param height - preview height
   *  @return suitable aspect ratio
   *
   *  source: https://github.com/android/camera-samples/blob/bfc46b68ef51f337104d3487c2394b204cf0d453/CameraXBasic/app/src/main/java/com/android/example/cameraxbasic/fragments/CameraFragment.kt#L350
   */
  private fun aspectRatio(width: Int, height: Int): Int {
    val previewRatio = max(width, height).toDouble() / min(width, height)
    if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
      return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
  }

  private fun setupShortCodeTextField() {
    with(shortCodeText) {
      filters = arrayOf(LengthFilter(SHORT_CODE_LENGTH))
      addTextChangedListener(ShortCodeSpanWatcher())
    }
  }

  private fun qrCodeScanned(qrCode: String) {
    qrScans.onNext(ScanSimpleIdScreenQrCodeScanned(qrCode))
  }

  private fun qrCodeChanges(): Observable<UiEvent> {
    return shortCodeText
        .textChangeEvents()
        .map { ShortCodeChanged }
  }

  private fun doneClicks(): Observable<UiEvent> {
    return shortCodeText
        .editorActionEvents { it.actionId == EditorInfo.IME_ACTION_SEARCH }
        .map { ShortCodeSearched(EnteredCodeInput(shortCodeText.text.toString())) }
  }

  private fun keyboardEvents(): Observable<UiEvent> {
    return Observable.create { emitter ->
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

  override fun showShortCodeValidationError(failure: EnteredCodeValidationResult) {
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
    previewView.visibility = View.GONE
    viewFinderImageView.visibility = View.GONE
  }

  override fun showQrCodeScannerView() {
    previewView.visibility = View.VISIBLE
    viewFinderImageView.visibility = View.VISIBLE
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
