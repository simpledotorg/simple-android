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
import com.spotify.mobius.functions.Consumer
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenHomeBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.change.FacilityChangeScreen
import org.simple.clinic.feature.Features
import org.simple.clinic.home.HomeScreen.ScreenRequest.ChangeCurrentFacility
import org.simple.clinic.home.HomeTab.OVERDUE
import org.simple.clinic.home.HomeTab.PATIENTS
import org.simple.clinic.home.HomeTab.REPORTS
import org.simple.clinic.home.help.HelpScreenKey
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.settings.SettingsScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class HomeScreen :
    BaseScreen<
        HomeScreenKey,
        ScreenHomeBinding,
        HomeScreenModel,
        HomeScreenEvent,
        HomeScreenEffect,
        Unit>(),
    HomeScreenUi,
    HomeScreenUiActions {

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

  @Inject
  lateinit var homeScreenUpdate: HomeScreenUpdate

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

  override fun defaultModel() = HomeScreenModel.create()

  override fun uiRenderer() = HomeScreenUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenHomeBinding.inflate(layoutInflater, container, false)

  override fun events() = facilitySelectionClicks()
      .compose(ReportAnalyticsEvents())
      .cast<HomeScreenEvent>()

  override fun createUpdate() = homeScreenUpdate

  override fun createInit() = HomeScreenInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) = effectHandlerFactory.create(this).build()

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

    viewPager.adapter = HomeScreenTabPagerAdapter(fragmentManager = childFragmentManager,
        lifecycle = viewLifecycleOwner.lifecycle,
        screens = tabs)
    TabLayoutMediator(homeTabLayout, viewPager) { tab, position ->
      tab.text = resources.getString(tabs[position].title)
    }.attach()

    // The WebView in "Progress" tab is expensive to load. Pre-instantiating
    // it when the app starts reduces its time-to-display.
    viewPager.offscreenPageLimit = REPORTS.ordinal - PATIENTS.ordinal
  }

  override fun onDestroyView() {
    viewPager.adapter = null
    super.onDestroyView()
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
    router.pushExpectingResult(ChangeCurrentFacility, FacilityChangeScreen.Key())
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

  interface Injector {
    fun inject(target: HomeScreen)
  }

  sealed class ScreenRequest : Parcelable {

    @Parcelize
    object ChangeCurrentFacility : ScreenRequest()
  }
}
