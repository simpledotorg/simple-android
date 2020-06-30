package org.simple.clinic.facility.change

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.screen_facility_change.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.confirm.ConfirmFacilityChangeSheet
import org.simple.clinic.facility.change.confirm.FacilityChangeComponent
import org.simple.clinic.location.LOCATION_PERMISSION
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.RecyclerViewUserScrollDetector
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.hideKeyboard
import java.util.Locale
import javax.inject.Inject

class FacilityChangeActivity : AppCompatActivity() {

  @Inject
  lateinit var controller: FacilityChangeActivityController

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()
  private val recyclerViewAdapter = ItemAdapter(FacilityListItem.Differ())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.screen_facility_change)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            searchQueryChanges(),
            facilityClicks(),
            locationPermissionChanges()
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
    toolbarWithSearch.setNavigationOnClickListener {
      finish()
    }
    toolbarWithoutSearch.setNavigationOnClickListener {
      finish()
    }

    facilityList.layoutManager = LinearLayoutManager(this)
    facilityList.adapter = recyclerViewAdapter

    // For some reasons, the keyboard stays
    // visible when coming from AppLockScreen.
    searchEditText.requestFocus()
    rootLayout.post { rootLayout.hideKeyboard() }

    hideKeyboardOnListScroll()
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun searchQueryChanges() =
      RxTextView
          .textChanges(searchEditText)
          .map { text -> FacilityChangeSearchQueryChanged(text.toString()) }

  private fun facilityClicks() =
      recyclerViewAdapter
          .itemEvents
          .ofType<FacilityListItem.FacilityItemClicked>()
          .map { FacilityChangeClicked(it.facility) }

  private fun locationPermissionChanges(): Observable<UiEvent> {
    val permissionResult = runtimePermissions.check(LOCATION_PERMISSION)
    return Observable.just(FacilityChangeLocationPermissionChanged(permissionResult))
  }

  @SuppressLint("CheckResult")
  private fun hideKeyboardOnListScroll() {
    val scrollEvents = RxRecyclerView.scrollEvents(facilityList)
    val scrollStateChanges = RxRecyclerView.scrollStateChanges(facilityList)

    Observables.combineLatest(scrollEvents, scrollStateChanges)
        .compose(RecyclerViewUserScrollDetector.streamDetections())
        .filter { it.byUser }
        .takeUntil(onDestroys)
        .subscribe {
          rootLayout.hideKeyboard()
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

  fun updateFacilities(facilityItems: List<FacilityListItem>, updateType: FacilitiesUpdateType) {
    recyclerViewAdapter.submitList(facilityItems)
  }

  private fun exitAfterChange() {
    val intent = Intent()
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  fun goBack() {
    val intent = Intent()
    setResult(Activity.RESULT_CANCELED, intent)
    finish()
  }

  fun showProgressIndicator() {
    progress.visibility = VISIBLE
  }

  fun hideProgressIndicator() {
    progress.visibility = GONE
  }

  fun showToolbarWithSearchField() {
    toolbarContainer.displayedChildResId = R.id.toolbarWithSearch
  }

  fun showToolbarWithoutSearchField() {
    toolbarContainer.displayedChildResId = R.id.toolbarWithoutSearch
  }

  fun openConfirmationSheet(facility: Facility) {
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
