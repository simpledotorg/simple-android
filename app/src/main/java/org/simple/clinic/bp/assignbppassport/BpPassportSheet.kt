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
import org.simple.clinic.databinding.SheetBpPassportBinding
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.facility.Facility
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import javax.inject.Inject

class BpPassportSheet : BottomSheetActivity(), BpPassportUiActions {

  companion object {
    private const val KEY_BP_PASSPORT_NUMBER = "bpPassportNumber"
    private const val FACILITY_CHANGE = "alertFacilityChange"

    fun selectedFacility(data: Intent): String? {
      return data.getStringExtra(FACILITY_CHANGE)
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
        defaultModel = BpPassportModel(),
        update = BpPassportUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = SheetBpPassportBinding.inflate(layoutInflater)
    setContentView(binding.root)

    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun openPatientEntryScreen(facility: Facility) {
    val intent = Intent()
    intent.putExtra(FACILITY_CHANGE, facility.name)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  override fun closeSheet() {
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
