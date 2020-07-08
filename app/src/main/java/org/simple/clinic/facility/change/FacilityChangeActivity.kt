package org.simple.clinic.facility.change

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.screen_facility_change.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.confirm.ConfirmFacilityChangeSheet
import org.simple.clinic.facility.change.confirm.FacilityChangeComponent
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class FacilityChangeActivity : AppCompatActivity(), FacilityChangeUi {

  @Inject
  lateinit var controller: FacilityChangeActivityController

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.screen_facility_change)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            facilityClicks()
        ),
        controller = controller,
        screenDestroys = onDestroys
    )

    setupUiComponents()
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDi()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { ViewPumpContextWrapper.wrap(it) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }

    super.attachBaseContext(wrappedContext)
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  private fun setupDi() {
    component = ClinicApp.appComponent
        .facilityChangeComponentBuilder()
        .activity(this)
        .build()

    component.inject(this)
  }

  private fun setupUiComponents() {
    facilityPickerView.backClicked = this@FacilityChangeActivity::finish
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

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

  companion object {
    lateinit var component: FacilityChangeComponent
    private const val OPEN_CONFIRMATION_SHEET = 1210

    fun intent(context: Context): Intent {
      return Intent(context, FacilityChangeActivity::class.java)
    }
  }
}
