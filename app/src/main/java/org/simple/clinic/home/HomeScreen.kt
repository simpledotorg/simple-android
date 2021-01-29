package org.simple.clinic.home

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenHomeBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.change.FacilityChangeScreen
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.home.HomeTab.OVERDUE
import org.simple.clinic.home.HomeTab.PATIENTS
import org.simple.clinic.home.HomeTab.REPORTS
import org.simple.clinic.home.help.HelpScreenKey
import org.simple.clinic.instantsearch.InstantSearchScreenKey
import org.simple.clinic.navigation.v2.ExpectsResult
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenResult
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.scanid.ScanSimpleIdScreen
import org.simple.clinic.search.PatientSearchScreenKey
import org.simple.clinic.settings.SettingsScreenKey
import org.simple.clinic.shortcodesearchresult.ShortCodeSearchResultScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.hideKeyboard
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class HomeScreen :
    BaseScreen<
        HomeScreenKey,
        ScreenHomeBinding,
        HomeScreenModel,
        HomeScreenEvent,
        HomeScreenEffect>(),
    HomeScreenUi,
    HomeScreenUiActions,
    ExpectsResult {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerFactory: HomeScreenEffectHandler.Factory

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var features: Features

  private val homeScreenRootLayout
    get() = binding.homeScreenRootLayout

  private val viewPager
    get() = binding.viewPager

  private val homeTabLayout
    get() = binding.homeTabLayout

  private val toolbar
    get() = binding.toolbar

  private val helpButton
    get() = binding.helpButton

  private val facilitySelectButton
    get() = binding.facilitySelectButton

  private val tabs = listOf(PATIENTS, OVERDUE, REPORTS)

  private val scanResults = PublishSubject.create<BusinessIdScanned>()

  override fun defaultModel() = HomeScreenModel.create()

  override fun uiRenderer() = HomeScreenUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenHomeBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .merge(
          facilitySelectionClicks(),
          scanResults
      )
      .compose(ReportAnalyticsEvents())
      .cast<HomeScreenEvent>()

  override fun createUpdate() = HomeScreenUpdate()

  override fun createInit() = HomeScreenInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupToolBar()
    setupHelpClicks()

    // Keyboard stays open after login finishes, not sure why.
    homeScreenRootLayout.hideKeyboard()

    viewPager.adapter = HomeScreenTabPagerAdapter(activity, tabs)
    TabLayoutMediator(homeTabLayout, viewPager) { tab, position ->
      tab.text = resources.getString(tabs[position].title)
    }.attach()

    // The WebView in "Progress" tab is expensive to load. Pre-instantiating
    // it when the app starts reduces its time-to-display.
    viewPager.offscreenPageLimit = REPORTS.ordinal - PATIENTS.ordinal
  }

  override fun onScreenResult(requestType: Parcelable, result: ScreenResult) {
    if (requestType is ScanPassportRequest && result is Succeeded) {
      val scanResult = ScanSimpleIdScreen.readScanResult(result)

      scanResults.onNext(BusinessIdScanned.fromScanResult(scanResult))
    }
  }

  private fun setupToolBar() {
    toolbar.apply {
      inflateMenu(R.menu.home)
      setOnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
          R.id.openSettings -> {
            router.push(SettingsScreenKey().wrap())
            true
          }
          else -> false
        }
      }
    }
  }

  private fun setupHelpClicks() {
    helpButton.setOnClickListener {
      router.push(HelpScreenKey().wrap())
    }
  }

  private fun facilitySelectionClicks() = facilitySelectButton
      .clicks()
      .map { HomeFacilitySelectionClicked }

  override fun setFacility(facilityName: String) {
    facilitySelectButton.text = facilityName
  }

  override fun openFacilitySelection() {
    activity.startActivity(FacilityChangeScreen.intent(requireContext()))
  }

  override fun showOverdueAppointmentCount(count: Int) {
    val overdueTabIndex = tabs.indexOf(OVERDUE)
    val overdueTab = homeTabLayout.getTabAt(overdueTabIndex)

    overdueTab?.run {
      @Suppress("UsePropertyAccessSyntax")
      getOrCreateBadge().apply {
        isVisible = true
        maxCharacterCount = 3
        number = count
      }
    }
  }

  override fun removeOverdueAppointmentCount() {
    val overdueTabIndex = tabs.indexOf(OVERDUE)
    val overdueTab = homeTabLayout.getTabAt(overdueTabIndex)

    overdueTab?.removeBadge()
  }

  override fun openShortCodeSearchScreen(shortCode: String) {
    router.push(ShortCodeSearchResultScreenKey(shortCode).wrap())
  }

  override fun openPatientSearchScreen(additionalIdentifier: Identifier?) {
    val screenKey = if (features.isEnabled(Feature.InstantSearch)) {
      InstantSearchScreenKey(additionalIdentifier)
    } else {
      PatientSearchScreenKey(additionalIdentifier).wrap()
    }

    router.push(screenKey)
  }

  override fun openPatientSummary(patientId: UUID) {
    router.push(PatientSummaryScreenKey(patientId, OpenIntention.ViewExistingPatient, Instant.now(utcClock)).wrap())
  }

  interface Injector {
    fun inject(target: HomeScreen)
  }

  @Parcelize
  object ScanPassportRequest : Parcelable
}
