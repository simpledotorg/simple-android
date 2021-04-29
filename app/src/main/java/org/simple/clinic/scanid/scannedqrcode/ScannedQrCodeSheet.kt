package org.simple.clinic.scanid.scannedqrcode

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetScannedQrCodeBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.patient.businessid.Identifier
import javax.inject.Inject

class ScannedQrCodeSheet :
    BaseBottomSheet<
        ScannedQrCodeSheet.Key,
        SheetScannedQrCodeBinding,
        ScannedQrCodeModel,
        ScannedQrCodeEvent,
        ScannedQrCodeEffect>(), ScannedQrCodeUiActions, ScannedQrCodeUi {

  companion object {

    fun blankBpPassportResult(result: Succeeded): BlankScannedQRCodeResult {
      return (result.result as BlankScannedQRCodeResult)
    }
  }

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: ScannedQrCodeEffectHandler.Factory

  private val registerNewPatientButton
    get() = binding.registerNewPatientButton

  private val addToExistingPatientButton
    get() = binding.addToExistingPatientButton

  private val patientIdentifierNumberTextView
    get() = binding.patientIdentifierNumberTextView

  private val identifier: Identifier by lazy {
    screenKey.identifier
  }

  override fun defaultModel() = ScannedQrCodeModel.create(identifier)

  override fun uiRenderer() = ScannedQrCodeUiRenderer(this)

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?): SheetScannedQrCodeBinding {
    return SheetScannedQrCodeBinding.inflate(layoutInflater, container, false)
  }

  override fun events(): Observable<ScannedQrCodeEvent> = Observable
      .merge(
          registerNewPatientClicks(),
          addToExistingPatientClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast()

  override fun createUpdate() = ScannedQrCodeUpdate()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun showBpPassportValue() {
    patientIdentifierNumberTextView.text = getString(R.string.sheet_bp_passport_number, identifier.displayValue())
  }

  override fun showIndianNationalHealthIdValue() {
    patientIdentifierNumberTextView.text = getString(R.string.sheet_national_health_id, identifier.displayValue())
  }

  override fun sendBpPassportResult(blankScannedQRCodeResult: BlankScannedQRCodeResult) {
    router.popWithResult(Succeeded(blankScannedQRCodeResult))
  }

  private fun addToExistingPatientClicks(): Observable<ScannedQrCodeEvent> {
    return addToExistingPatientButton
        .clicks()
        .map { AddToExistingPatientClicked }
  }

  private fun registerNewPatientClicks(): Observable<ScannedQrCodeEvent> {
    return registerNewPatientButton
        .clicks()
        .map { RegisterNewPatientClicked }
  }

  @Parcelize
  data class Key(
      val identifier: Identifier
  ) : ScreenKey() {

    override val analyticsName = "Blank BP passport sheet"

    override fun instantiateFragment() = ScannedQrCodeSheet()

    override val type = ScreenType.Modal
  }

  interface Injector {
    fun inject(target: ScannedQrCodeSheet)
  }
}
