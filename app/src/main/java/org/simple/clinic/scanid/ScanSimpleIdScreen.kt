package org.simple.clinic.scanid

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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
import org.simple.clinic.feature.Feature.IndiaNationalHealthID
import org.simple.clinic.feature.Features
import org.simple.clinic.instantsearch.InstantSearchScreenKey
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.qrcodeanalyzer.MLKitQrCodeAnalyzer
import org.simple.clinic.scanid.qrcodeanalyzer.ZxingQrCodeAnalyzer
import org.simple.clinic.shortcodesearchresult.ShortCodeSearchResultScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.BitmapUtils
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors
import javax.inject.Inject

class ScanSimpleIdScreen : BaseScreen<
    ScanSimpleIdScreenKey,
    ScreenScanSimpleBinding,
    ScanSimpleIdModel,
    ScanSimpleIdEvent,
    ScanSimpleIdEffect>(), ScanSimpleIdUi, ScanSimpleIdUiActions {

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var effectHandlerFactory: ScanSimpleIdEffectHandler.Factory

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var bitmapUtils: BitmapUtils

  @Inject
  lateinit var googleApiAvailability: GoogleApiAvailability

  private val toolBar
    get() = binding.toolBar

  private val enteredCodeText
    get() = binding.enteredCodeText

  private val enteredCodeErrorText
    get() = binding.shortCodeErrorText

  private val searchingContainer
    get() = binding.searchingContainer

  private val previewView
    get() = binding.previewView

  private val viewFinderImageView
    get() = binding.viewFinderImageView

  private val scanErrorTextView
    get() = binding.scanErrorTextView

  private val cameraController by unsafeLazy {
    LifecycleCameraController(requireContext()).apply {
      isTapToFocusEnabled = true
    }
  }

  private val keyboardVisibilityDetector = KeyboardVisibilityDetector()
  private val cameraExecutor = Executors.newSingleThreadExecutor()

  private val qrScans = PublishSubject.create<ScanSimpleIdEvent>()

  override fun defaultModel() = ScanSimpleIdModel.create()

  override fun uiRenderer() = ScanSimpleIdUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenScanSimpleBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .mergeArray(qrScans.distinctUntilChanged(), keyboardEvents(), qrCodeChanges(), doneClicks())
      .compose(ReportAnalyticsEvents())
      .cast<ScanSimpleIdEvent>()

  override fun createUpdate() = ScanSimpleIdUpdate(crashReporter, features.isEnabled(IndiaNationalHealthID))

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

    cameraController.bindToLifecycle(this)
    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
    previewView.controller = cameraController

    configCameraController()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    cameraController.unbind()
  }

  private fun configCameraController() {
    cameraController.cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    val googlePlayServicesAvailability = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
    val isGooglePlayServicesAvailable = googlePlayServicesAvailability == ConnectionResult.SUCCESS

    val isMLKitQrCodeScannerEnabled = features.isEnabled(Feature.MLKitQrCodeScanner)
    val qrCodeAnalyzer = if (isMLKitQrCodeScannerEnabled && isGooglePlayServicesAvailable) {
      MLKitQrCodeAnalyzer(bitmapUtils, ::qrCodeScanned, mlKitUnavailable = {
        setQrCodeAnalyzer(ZxingQrCodeAnalyzer(::qrCodeScanned))
      })
    } else {
      ZxingQrCodeAnalyzer(::qrCodeScanned)
    }

    setQrCodeAnalyzer(qrCodeAnalyzer)
  }

  private fun setQrCodeAnalyzer(qrCodeAnalyzer: ImageAnalysis.Analyzer) {
    cameraController.clearImageAnalysisAnalyzer()
    cameraController.setImageAnalysisAnalyzer(cameraExecutor, qrCodeAnalyzer)
  }

  private fun qrCodeScanned(qrCode: String) {
    qrScans.onNext(ScanSimpleIdScreenQrCodeScanned(qrCode))
  }

  private fun qrCodeChanges(): Observable<UiEvent> {
    return enteredCodeText
        .textChangeEvents()
        .map { EnteredCodeChanged }
  }

  private fun doneClicks(): Observable<UiEvent> {
    return enteredCodeText
        .editorActionEvents { it.actionId == EditorInfo.IME_ACTION_SEARCH }
        .map { EnteredCodeSearched(EnteredCodeInput(enteredCodeText.text.toString())) }
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

  override fun openPatientSummary(patientId: UUID) {
    router.replaceTop(PatientSummaryScreenKey(
        patientUuid = patientId,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)))
  }

  override fun openShortCodeSearch(shortCode: String) {
    router.replaceTop(ShortCodeSearchResultScreenKey(shortCode))
  }

  override fun openPatientSearch(additionalIdentifier: Identifier?, initialSearchQuery: String?) {
    val keyToPush = InstantSearchScreenKey(additionalIdentifier = additionalIdentifier,
        initialSearchQuery = null)

    when (val openedFrom = screenKey.openedFrom) {
      OpenedFrom.InstantSearchScreen -> router.replaceKeyOfSameType(keyToPush)
      OpenedFrom.PatientsTabScreen -> router.replaceTop(keyToPush)
      else -> throw IllegalArgumentException("Opened from unknown: $openedFrom")
    }
  }

  override fun showEnteredCodeValidationError(failure: EnteredCodeValidationResult) {
    enteredCodeErrorText.visibility = View.VISIBLE
    val validationErrorMessage = if (failure == Empty) {
      R.string.scansimpleid_enteredcode_error_empty
    } else {
      R.string.scansimpleid_enteredcode_error_not_required_length
    }
    enteredCodeErrorText.text = resources.getString(validationErrorMessage)
  }

  override fun hideEnteredCodeValidationError() {
    enteredCodeErrorText.visibility = View.GONE
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

  override fun hideScanError() {
    scanErrorTextView.visibility = View.GONE
  }

  override fun showScanError() {
    scanErrorTextView.visibility = View.VISIBLE
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
