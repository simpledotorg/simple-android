package org.simple.clinic.scheduleappointment.facilityselection

import android.content.Context
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.screen_patient_facility_change.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.facility.change.FacilitiesUpdateType
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.location.LOCATION_PERMISSION
import org.simple.clinic.registration.facility.FacilitiesAdapter
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class FacilitySelectionActivity : AppCompatActivity() {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var controller: FacilitySelectionActivityController

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  private lateinit var component: FacilitySelectionActivityComponent

  private val recyclerViewAdapter = FacilitiesAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.screen_patient_facility_change)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            searchQueryChanges(),
            locationPermissionChanges(),
            facilityClicks()
        ),
        controller = controller,
        screenDestroys = onDestroys
    )

    facilityList.layoutManager = LinearLayoutManager(this)
    facilityList.adapter = recyclerViewAdapter
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
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  private fun screenCreates() = Observable.just<UiEvent>(ScreenCreated())

  private fun searchQueryChanges() =
      RxTextView
          .textChanges(searchQueryEditText)
          .map { text -> FacilitySelectionSearchQueryChanged(text.toString()) }

  private fun locationPermissionChanges(): Observable<UiEvent> {
    val permissionResult = RuntimePermissions.check(this, LOCATION_PERMISSION)
    return Observable.just(FacilitySelectionLocationPermissionChanged(permissionResult))
  }

  private fun facilityClicks() =
      recyclerViewAdapter
          .facilityClicks
          .map { FacilitySelected(it) }

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

  fun showProgressIndicator() {
    progress.visibility = RelativeLayout.VISIBLE
  }

  fun hideProgressIndicator() {
    progress.visibility = RelativeLayout.GONE
  }

  fun sendSelectedFacility(selectedFacilityUuid: UUID) {
    TODO("not implemented")
  }
}
