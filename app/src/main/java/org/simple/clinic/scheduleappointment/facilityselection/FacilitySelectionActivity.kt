package org.simple.clinic.scheduleappointment.facilityselection

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ClinicApp
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ActivitySelectFacilityBinding
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.facility.Facility
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class FacilitySelectionActivity : AppCompatActivity(), FacilitySelectionUi, FacilitySelectionUiActions {

  companion object {
    const val EXTRA_SELECTED_FACILITY = "selected_facility"

    fun selectedFacility(data: Intent): Facility {
      return data.getParcelableExtra(EXTRA_SELECTED_FACILITY)!!
    }
  }

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var effectHandlerFactory: FacilitySelectionEffectHandler.Factory

  private lateinit var binding: ActivitySelectFacilityBinding

  private val facilityPickerView
    get() = binding.facilityPickerView

  private val events by unsafeLazy {
    facilityClicks()
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = FacilitySelectionUiRenderer(this)

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = FacilitySelectionModel(),
        update = FacilitySelectionUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = FacilitySelectionInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private lateinit var component: FacilitySelectionActivityComponent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivitySelectFacilityBinding.inflate(layoutInflater)
    setContentView(binding.root)

    facilityPickerView.backClicked = this@FacilitySelectionActivity::finish

    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun attachBaseContext(baseContext: Context) {
    component = ClinicApp
        .appComponent
        .patientFacilityChangeComponentBuilder()
        .activity(this)
        .build()
    component.inject(this)

    val wrappedContext = baseContext
        .wrap { ViewPumpContextWrapper.wrap(it) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(locale, features))
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  private fun facilityClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      facilityPickerView.facilitySelectedCallback = { emitter.onNext(FacilitySelected(it)) }
    }
  }

  override fun sendSelectedFacility(selectedFacility: Facility) {
    val intent = Intent()
    intent.putExtra(EXTRA_SELECTED_FACILITY, selectedFacility)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }
}
