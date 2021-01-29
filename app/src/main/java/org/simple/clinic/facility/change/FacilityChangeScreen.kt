package org.simple.clinic.facility.change

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.ClinicApp
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenFacilityChangeBinding
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.confirm.ConfirmFacilityChangeSheet
import org.simple.clinic.facility.change.confirm.FacilityChangeComponent
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class FacilityChangeScreen :
    BaseScreen<
        FacilityChangeScreen.Key,
        ScreenFacilityChangeBinding,
        FacilityChangeModel,
        FacilityChangeEvent,
        FacilityChangeEffect>(),
    FacilityChangeUi,
    FacilityChangeUiActions {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var effectHandlerFactory: FacilityChangeEffectHandler.Factory

  @Inject
  lateinit var features: Features

  override fun defaultModel() = FacilityChangeModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenFacilityChangeBinding.inflate(layoutInflater, container, false)

  override fun events() = facilityClicks()
      .compose(ReportAnalyticsEvents())
      .cast<FacilityChangeEvent>()

  override fun createInit() = FacilityChangeInit()

  override fun createUpdate() = FacilityChangeUpdate()

  override fun uiRenderer() = FacilityChangeUiRenderer(this)

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  private val facilityPickerView
    get() = binding.facilityPickerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ScreenFacilityChangeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupUiComponents()
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDi()

    val wrappedContext = baseContext
        .wrap { ViewPumpContextWrapper.wrap(it) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(locale, features))
  }

  private fun setupDi() {
    component = ClinicApp.appComponent
        .facilityChangeComponent()
        .create(activity = this)

    component.inject(this)
  }

  private fun setupUiComponents() {
    facilityPickerView.backClicked = this@FacilityChangeScreen::finish
  }

  private fun facilityClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      facilityPickerView.facilitySelectedCallback = { emitter.onNext(FacilityChangeClicked(it)) }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == OPEN_CONFIRMATION_SHEET && resultCode == Activity.RESULT_OK) {
      exitAfterChange()
    } else {
      goBack()
    }
  }

  private fun exitAfterChange() {
    val intent = Intent()
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  override fun goBack() {
    val intent = Intent()
    setResult(Activity.RESULT_CANCELED, intent)
    finish()
  }

  override fun openConfirmationSheet(facility: Facility) {
    startActivityForResult(
        ConfirmFacilityChangeSheet.intent(this, facility),
        OPEN_CONFIRMATION_SHEET
    )
  }

  @Parcelize
  class Key : ScreenKey() {

    override val analyticsName = "Change Facility"

    override fun instantiateFragment() = FacilityChangeScreen()
  }

  companion object {
    lateinit var component: FacilityChangeComponent
    private const val OPEN_CONFIRMATION_SHEET = 1210

    fun intent(context: Context): Intent {
      return Intent(context, FacilityChangeScreen::class.java)
    }
  }
}
