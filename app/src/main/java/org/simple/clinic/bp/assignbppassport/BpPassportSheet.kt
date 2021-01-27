package org.simple.clinic.bp.assignbppassport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.databinding.SheetBpPassportBinding
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.patient.businessid.Identifier
import javax.inject.Inject

class BpPassportSheet :
    BaseBottomSheet<
        BpPassportSheet.Key,
        SheetBpPassportBinding,
        BpPassportModel,
        BpPassportEvent,
        BpPassportEffect>(), BpPassportUiActions {

  companion object {

    fun blankBpPassportResult(result: Succeeded): BlankBpPassportResult {
      return (result.result as BlankBpPassportResult)
    }
  }

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: BpPassportEffectHandler.Factory

  private val registerNewPatientButton
    get() = binding.registerNewPatientButton

  private val addToExistingPatientButton
    get() = binding.addToExistingPatientButton

  private val bpPassportNumberTextview
    get() = binding.bpPassportNumberTextview

  private val bpPassportIdentifier: Identifier by lazy {
    screenKey.identifier
  }

  override fun defaultModel() = BpPassportModel.create(bpPassportIdentifier)

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?): SheetBpPassportBinding {
    return SheetBpPassportBinding.inflate(layoutInflater, container, false)
  }

  override fun events(): Observable<BpPassportEvent> {
    return Observable
        .merge(
            registerNewPatientClicks(),
            addToExistingPatientClicks()
        )
  }

  override fun createUpdate() = BpPassportUpdate()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    bpPassportNumberTextview.text = getString(R.string.sheet_bp_passport_number, bpPassportIdentifier.displayValue())
  }

  override fun sendBpPassportResult(blankBpPassportResult: BlankBpPassportResult) {
    router.popWithResult(Succeeded(blankBpPassportResult))
  }

  private fun addToExistingPatientClicks(): Observable<BpPassportEvent> {
    return addToExistingPatientButton
        .clicks()
        .map { AddToExistingPatientClicked }
  }

  private fun registerNewPatientClicks(): Observable<BpPassportEvent> {
    return registerNewPatientButton
        .clicks()
        .map { RegisterNewPatientClicked }
  }

  @Parcelize
  data class Key(
      val identifier: Identifier
  ) : ScreenKey() {

    override val analyticsName = "Blank BP passport sheet"

    override fun instantiateFragment() = BpPassportSheet()

    override val type = ScreenType.Modal
  }
}
