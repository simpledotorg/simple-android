package org.simple.clinic.scanid

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.editorActionEvents
import com.jakewharton.rxbinding3.widget.textChangeEvents
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenScanSimpleBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Features
import org.simple.clinic.instantsearch.InstantSearchScreenKey
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.PatientPrefillInfo
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.qrcodeanalyzer.MLKitQrCodeAnalyzer
import org.simple.clinic.scanid.qrcodeanalyzer.ZxingQrCodeAnalyzer
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.BitmapUtils
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.applyStatusBarPadding
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
    ScanSimpleIdEffect,
    ScanSimpleIdViewEffect>(), ScanSimpleIdUi, ScanSimpleIdUiActions {

  companion object {
    fun readIdentifier(result: Succeeded): Identifier {
      return (result.result as ScannedIdentifier).identifier
    }
  }

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var effectHandlerFactory: ScanSimpleIdEffectHandler.Factory

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var bitmapUtils: BitmapUtils

  @Inject
  lateinit var googleApiAvailability: GoogleApiAvailability

  @Inject
  lateinit var viewEffectHandlerFactory: ScanSimpleIdViewEffectHandler.Factory

  private val appbar
    get() = binding.appbarLayout

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

  private val enteredCodeContainer
    get() = binding.enteredCodeContainer

  private val keyboardVisibilityDetector = KeyboardVisibilityDetector()
  private val cameraExecutor = Executors.newSingleThreadExecutor()
  private val cameraProviderFuture by unsafeLazy {
    ProcessCameraProvider.getInstance(requireContext())
  }

  private val qrScans = PublishSubject.create<ScanSimpleIdEvent>()

  override fun defaultModel() = ScanSimpleIdModel.create(screenKey.openedFrom)

  override fun uiRenderer() = ScanSimpleIdUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenScanSimpleBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .mergeArray(qrScans.distinctUntilChanged(), keyboardEvents(), qrCodeChanges(), doneClicks())
      .compose(ReportAnalyticsEvents())
      .cast<ScanSimpleIdEvent>()

  override fun createUpdate() = ScanSimpleIdUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<ScanSimpleIdViewEffect>) = effectHandlerFactory.create(viewEffectsConsumer).build()

  override fun viewEffectHandler() = viewEffectHandlerFactory.create(this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // It is possible that going back via the app bar from future screens will come back to this
    // screen with the keyboard open. So, we hide it here.
    binding.root.hideKeyboard()
    appbar.applyStatusBarPadding()
    toolBar.setNavigationOnClickListener { router.pop() }

    cameraProviderFuture.addListener({
      val cameraProvider = cameraProviderFuture.get()
      startCamera(cameraProvider)
    }, ContextCompat.getMainExecutor(requireContext()))
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun startCamera(cameraProvider: ProcessCameraProvider) {
    val rotation = previewView.display.rotation

    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    val resolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
        .build()

    val preview = Preview.Builder()
        .setResolutionSelector(resolutionSelector)
        .setTargetRotation(rotation)
        .build()

    val analyzer = ImageAnalysis.Builder()
        .setResolutionSelector(resolutionSelector)
        .setTargetRotation(rotation)
        .build()

    val googlePlayServicesAvailability = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
    val isGooglePlayServicesAvailable = googlePlayServicesAvailability == ConnectionResult.SUCCESS

    val qrCodeAnalyzer = if (isGooglePlayServicesAvailable) {
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

  override fun openPatientSearch(additionalIdentifier: Identifier?, initialSearchQuery: String?, patientPrefillInfo: PatientPrefillInfo?) {
    val keyToPush = InstantSearchScreenKey(additionalIdentifier = additionalIdentifier,
        initialSearchQuery = initialSearchQuery,
        patientPrefillInfo = patientPrefillInfo)

    when (val openedFrom = screenKey.openedFrom) {
      OpenedFrom.InstantSearchScreen -> router.replaceKeyOfSameType(keyToPush)
      OpenedFrom.PatientsTabScreen -> router.replaceTop(keyToPush)
      else -> throw IllegalArgumentException("Opened from unknown: $openedFrom")
    }
  }

  override fun goBackToEditPatientScreen(identifier: Identifier) {
    router.popWithResult(Succeeded(ScannedIdentifier(identifier)))
  }

  override fun showPatientWithIdentifierExistsError() {
    Snackbar
        .make(binding.root, R.string.scansimpleid_identfier_already_exists, Snackbar.LENGTH_SHORT)
        .setAction(R.string.scansimpleid_error_state_snackbar_ok) {}
        .show()
  }

  override fun showInvalidQrCodeError() {
    Snackbar
        .make(binding.root, R.string.scansimpleid_invalid_qr_code, Snackbar.LENGTH_SHORT)
        .setAction(R.string.scansimpleid_error_state_snackbar_ok) {}
        .show()
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

  override fun hideEnteredCodeContainerView() {
    enteredCodeContainer.visibility = View.GONE
  }

  override fun setToolBarTitle(openedFrom: OpenedFrom) {
    toolBar.title = when (openedFrom) {
      OpenedFrom.InstantSearchScreen, OpenedFrom.PatientsTabScreen -> resources.getString(R.string.scansimpleid_bp_passport_or_national_health_id)
      OpenedFrom.EditPatientScreen.ToAddBpPassport -> resources.getString(R.string.scansimpleid_bp_passport_title)
      OpenedFrom.EditPatientScreen.ToAddNHID -> resources.getString(R.string.scansimpleid_national_id_title)
    }
  }

  interface Injector {
    fun inject(target: ScanSimpleIdScreen)
  }

  @Parcelize
  data class ScannedIdentifier(val identifier: Identifier) : Parcelable
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
