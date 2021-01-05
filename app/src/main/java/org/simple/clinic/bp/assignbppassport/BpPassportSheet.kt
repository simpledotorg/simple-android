package org.simple.clinic.bp.assignbppassport

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import com.jakewharton.rxbinding3.view.clicks
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.databinding.SheetBpPassportBinding
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import javax.inject.Inject

class BpPassportSheet : BottomSheetActivity(), BpPassportUiActions {

  companion object {
    private const val KEY_BP_PASSPORT_NUMBER = "bpPassportNumber"
    private const val BP_PASSPORT_RESULT = "bpPassportResult"

    fun intent(
        context: Context,
        bpPassportNumber: Identifier
    ): Intent {
      val intent = Intent(context, BpPassportSheet::class.java)
      intent.putExtra(KEY_BP_PASSPORT_NUMBER, bpPassportNumber)
      return intent
    }

    fun blankBpPassportResult(data: Intent): String? {
      return data.getStringExtra(BP_PASSPORT_RESULT)
    }
  }

  @Inject
  lateinit var effectHandlerFactory: BpPassportEffectHandler.Factory

  private lateinit var component: BpPassportSheetComponent

  private lateinit var binding: SheetBpPassportBinding

  private val registerNewPatientButton
    get() = binding.registerNewPatientButton

  private val addToExistingPatientButton
    get() = binding.addToExistingPatientButton

  private val bpPassportNumberTextview
    get() = binding.bpPassportNumberTextview

  private val events: Observable<BpPassportEvent> by unsafeLazy {
    Observable
        .merge(
            registerNewPatientClicks(),
            addToExistingPatientClicks()
        )
  }

  private val delegate by unsafeLazy {

    MobiusDelegate.forActivity(
        events = events,
        defaultModel = BpPassportModel.create(identifier = bpPassportIdentifier),
        update = BpPassportUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
    )
  }

  private val bpPassportIdentifier: Identifier by lazy {
    intent.getParcelableExtra(KEY_BP_PASSPORT_NUMBER)!!
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = SheetBpPassportBinding.inflate(layoutInflater)
    setContentView(binding.root)

    bpPassportNumberTextview.text = getString(R.string.sheet_bp_passport_number, bpPassportIdentifier.displayValue())
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun sendBpPassportResult(blankBpPassportResult: BlankBpPassportResult) {
    val intent = Intent()
    intent.putExtra(BP_PASSPORT_RESULT, blankBpPassportResult)
    setResult(Activity.RESULT_OK, intent)
    finish()
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

  private fun setUpDiGraph() {
    component = ClinicApp.appComponent
        .bpPassportSheetComponent()
        .activity(this)
        .build()

    component.inject(this)
  }

  override fun attachBaseContext(newBase: Context) {
    setUpDiGraph()

    val wrappedContext = newBase
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    super.onStop()
    delegate.stop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }
}
