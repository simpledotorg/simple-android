package org.simple.clinic.scheduleappointment.patientFacilityTransfer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.screen_patient_facility_change.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.facility.change.FacilitiesUpdateType
import org.simple.clinic.facility.change.FacilityChangeLocationPermissionChanged
import org.simple.clinic.facility.change.FacilityChangeSearchQueryChanged
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.location.LOCATION_PERMISSION
import org.simple.clinic.registration.facility.FacilitiesAdapter
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import java.util.Locale
import javax.inject.Inject

class PatientFacilityChangeActivity : AppCompatActivity() {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var controller: PatientFacilityChangeController

  private val recyclerViewAdapter = FacilitiesAdapter()

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  private val screenResults = ScreenResultBus()

  private lateinit var component: PatientFacilityChangeComponent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.screen_patient_facility_change)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            searchQueryChanges(),
            locationPermissionChanges()
        ),
        controller = controller,
        screenDestroys = onDestroys
    )
  }

  override fun attachBaseContext(baseContext: Context) {
    component = ClinicApp
        .appComponent
        .patientFacilityChangeComponentBuilder()
        .activity(this)
        .build()
    component.inject(this)

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    screenResults.send(ActivityResult(requestCode, resultCode, data))
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    screenResults.send(ActivityPermissionResult(requestCode))
  }

  private fun screenCreates() = Observable.just<UiEvent>(ScreenCreated())

  private fun searchQueryChanges() =
      RxTextView
          .textChanges(searchQueryEditText)
          .map { text -> FacilityChangeSearchQueryChanged(text.toString()) }

  private fun locationPermissionChanges(): Observable<UiEvent> {
    val permissionResult = RuntimePermissions.check(this, LOCATION_PERMISSION)
    return Observable.just(FacilityChangeLocationPermissionChanged(permissionResult))
  }

  fun updateFacilities(facilityItems: List<FacilityListItem>, updateType: FacilitiesUpdateType) {
    // Avoid animating the items on their first entry.
    facilityList.itemAnimator = when (updateType) {
      FacilitiesUpdateType.FIRST_UPDATE -> null
      FacilitiesUpdateType.SUBSEQUENT_UPDATE -> SlideUpAlphaAnimator()
          .withInterpolator(FastOutSlowInInterpolator())
          .apply { moveDuration = 200 }
    }

    facilityList.scrollToPosition(0)
    recyclerViewAdapter.submitList(facilityItems)
  }

  fun showToolbarWithSearchField() {
    toolbarContainer.displayedChildResId = toolbarWithSearch.id
  }

  fun showToolbarWithoutSearchField() {
    toolbarContainer.displayedChildResId = toolbarWithoutSearch.id
  }

  fun goBack() {
    TODO("not implemented")
  }

  fun showProgressIndicator() {
    progress.visibility = RelativeLayout.VISIBLE
  }

  fun hideProgressIndicator() {
    progress.visibility = RelativeLayout.GONE
  }
}
