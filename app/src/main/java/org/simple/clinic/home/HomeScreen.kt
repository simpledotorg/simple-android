package org.simple.clinic.home

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_home.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.facility.change.FacilityChangeActivity
import org.simple.clinic.home.HomeTab.OVERDUE
import org.simple.clinic.home.HomeTab.PATIENTS
import org.simple.clinic.home.HomeTab.REPORTS
import org.simple.clinic.home.help.HelpScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreenKey
import org.simple.clinic.settings.SettingsScreenKey
import org.simple.clinic.shortcodesearchresult.ShortCodeSearchResultScreenKey
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class HomeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), HomeScreenUi, HomeScreenUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerFactory: HomeScreenEffectHandler.Factory

  private val tabs = listOf(PATIENTS, OVERDUE, REPORTS)

  private val events by unsafeLazy {
    facilitySelectionClicks()
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = HomeScreenUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = HomeScreenModel.create(),
        init = HomeScreenInit(),
        update = HomeScreenUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

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
    viewPager.offscreenPageLimit = HomeTab.REPORTS.ordinal - HomeTab.PATIENTS.ordinal
  }

  private fun setupToolBar() {
    toolbar.apply {
      inflateMenu(R.menu.home)
      setOnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
          R.id.openSettings -> {
            screenRouter.push(SettingsScreenKey())
            true
          }
          else -> false
        }
      }
    }
  }

  private fun setupHelpClicks() {
    helpButton.setOnClickListener {
      screenRouter.push(HelpScreenKey())
    }
  }

  private fun facilitySelectionClicks() = facilitySelectButton
      .clicks()
      .map { HomeFacilitySelectionClicked }

  override fun setFacility(facilityName: String) {
    facilitySelectButton.text = facilityName
  }

  override fun openFacilitySelection() {
    activity.startActivity(FacilityChangeActivity.intent(context))
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
    screenRouter.push(ShortCodeSearchResultScreenKey(shortCode))
  }

  override fun openPatientSearchScreen(additionalIdentifier: Identifier?) {
    screenRouter.push(PatientSearchScreenKey(additionalIdentifier))
  }

  interface Injector {
    fun inject(target: HomeScreen)
  }
}
