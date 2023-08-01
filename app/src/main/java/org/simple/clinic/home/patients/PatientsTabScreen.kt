package org.simple.clinic.home.patients

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.PLAY_STORE_URL_FOR_SIMPLE
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.ActivityLifecycle
import org.simple.clinic.activity.ActivityLifecycle.Resumed
import org.simple.clinic.activity.permissions.RequestPermissions
import org.simple.clinic.activity.permissions.RuntimePermissions
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appupdate.AppUpdateNudgePriority
import org.simple.clinic.appupdate.criticalupdatedialog.CriticalAppUpdateDialog
import org.simple.clinic.appupdate.dialog.AppUpdateDialog
import org.simple.clinic.databinding.ScreenPatientsBinding
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.MonthAndYear
import org.simple.clinic.di.injector
import org.simple.clinic.drugstockreminders.DrugStockNotificationScheduler
import org.simple.clinic.drugstockreminders.enterdrugstock.EnterDrugStockScreen
import org.simple.clinic.enterotp.EnterOtpScreen
import org.simple.clinic.feature.Feature.MonthlyDrugStockReportReminder
import org.simple.clinic.feature.Feature.NotifyAppUpdateAvailableV2
import org.simple.clinic.feature.Features
import org.simple.clinic.instantsearch.InstantSearchScreenKey
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.monthlyreports.list.MonthlyReportsScreen
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.questionnaire.DrugStockReports
import org.simple.clinic.scanid.OpenedFrom
import org.simple.clinic.scanid.ScanSimpleIdScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.RuntimeNetworkStatus
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.indexOfChildId
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class PatientsTabScreen : BaseScreen<
    PatientsTabScreen.Key,
    ScreenPatientsBinding,
    PatientsTabModel,
    PatientsTabEvent,
    PatientsTabEffect,
    PatientsTabViewEffect>(), PatientsTabUi, PatientsTabUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var activityLifecycle: Observable<ActivityLifecycle>

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var country: Country

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var effectHandlerFactory: PatientsEffectHandler.Factory

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var userClock: UserClock

  @Inject
  @DateFormatter(MonthAndYear)
  lateinit var monthYearDateFormatter: DateTimeFormatter

  @Inject
  lateinit var drugStockNotificationScheduler: DrugStockNotificationScheduler

  @Inject
  lateinit var runtimeNetworkStatus: RuntimeNetworkStatus<UiEvent>

  private val deferredEvents = DeferredEventSource<PatientsTabEvent>()

  @IdRes
  private var currentStatusViewId: Int = R.id.userStatusHiddenView

  private val homeIllustration
    get() = binding.homeIllustration

  private val userStatusViewflipper
    get() = binding.userStatusViewflipper

  private val searchPatientsButton
    get() = binding.searchPatientsButton

  private val userStatusApproved
    get() = binding.userStatusApproved

  private val dismissApprovedStatusButton
    get() = userStatusApproved.dismissApprovedStatusButton

  private val userAwaitingSmsVerification
    get() = binding.userAwaitingSmsVerification

  private val enterCodeButton
    get() = userAwaitingSmsVerification.enterCodeButton

  private val scanSimpleCardButton
    get() = binding.scanSimpleCardButton

  private val syncIndicator
    get() = binding.syncIndicator

  private val illustrationLayout
    get() = binding.illustrationLayout

  private val appUpdateCardLayout
    get() = binding.appUpdateCardLayout

  private val appUpdateCardUpdateNowButton
    get() = appUpdateCardLayout.updateNowButton

  private val appUpdateCardUpdateReason
    get() = appUpdateCardLayout.criticalUpdateReason

  private val drugStockReminderCardLayout
    get() = binding.drugStockReminderCardLayout

  private val drugStockReminderCardSubTitle
    get() = drugStockReminderCardLayout.drugStockReminderCardSubTitle

  private val enterDrugStockButton
    get() = drugStockReminderCardLayout.enterDrugStockButton

  override fun defaultModel() = PatientsTabModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenPatientsBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = PatientsTabUiRenderer(
      ui = this,
      currentDate = LocalDate.now(userClock)
  )

  override fun viewEffectHandler() = PatientsTabViewEffectHandler(this)

  override fun events() = Observable
      .mergeArray(
          activityResumes(),
          searchButtonClicks(),
          dismissApprovedStatusClicks(),
          enterCodeManuallyClicks(),
          scanCardIdButtonClicks(),
          appUpdateCardUpdateNowClicked(),
          enterDrugStockClicked(),
      )
      .compose<UiEvent>(RequestPermissions(runtimePermissions, screenResults.streamResults().ofType()))
      .compose(runtimeNetworkStatus::apply)
      .compose(ReportAnalyticsEvents())
      .cast<PatientsTabEvent>()

  override fun createUpdate() = PatientsTabUpdate(features.isEnabled(NotifyAppUpdateAvailableV2), country)

  override fun createInit() = PatientsInit(
      isNotifyAppUpdateAvailableV2Enabled = features.isEnabled(NotifyAppUpdateAvailableV2),
      isMonthlyDrugStockReportReminderEnabledInIndia = features.isEnabled(MonthlyDrugStockReportReminder) && country.isoCountryCode == Country.INDIA,
  )

  override fun createEffectHandler(viewEffectsConsumer: Consumer<PatientsTabViewEffect>) = effectHandlerFactory.create(
      viewEffectsConsumer = viewEffectsConsumer
  ).build()

  override fun additionalEventSources() = listOf(deferredEvents)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupApprovalStatusAnimations()

    homeIllustration.setImageResource(illustrationResourceId())

    val isMonthlyDrugStockReminderEnabledInIndia = features.isEnabled(MonthlyDrugStockReportReminder) && country.isoCountryCode == Country.INDIA
    if (isMonthlyDrugStockReminderEnabledInIndia) {
      drugStockNotificationScheduler.schedule()
    }
  }

  private fun illustrationResourceId(): Int =
      when (country.isoCountryCode) {
        Country.INDIA -> R.drawable.illustration_homescreen_india
        Country.BANGLADESH -> R.drawable.illustration_homescreen_bangladesh
        Country.ETHIOPIA -> R.drawable.illustration_homescreen_ethiopia
        Country.SRI_LANKA -> R.drawable.illustration_homescreen_sri_lanka
        else -> R.drawable.illustration_homescreen_default
      }

  private fun setupApprovalStatusAnimations() {
    val entryAnimation = AnimationUtils.loadAnimation(context, R.anim.user_approval_status_entry)
    userStatusViewflipper.inAnimation = entryAnimation.apply { interpolator = FastOutSlowInInterpolator() }

    val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.user_approval_status_exit)
    userStatusViewflipper.outAnimation = exitAnimation.apply { interpolator = FastOutSlowInInterpolator() }
  }

  private fun activityResumes() = activityLifecycle
      .ofType<Resumed>()
      .map { ActivityResumed }

  private fun searchButtonClicks() = searchPatientsButton.clicks().map { NewPatientClicked }

  private fun dismissApprovedStatusClicks() = dismissApprovedStatusButton.clicks().map { UserApprovedStatusDismissed() }

  private fun enterCodeManuallyClicks() = enterCodeButton.clicks().map { PatientsEnterCodeManuallyClicked() }

  private fun scanCardIdButtonClicks() = scanSimpleCardButton.clicks().map { ScanCardIdButtonClicked() }

  private fun appUpdateCardUpdateNowClicked() = appUpdateCardUpdateNowButton
      .clicks()
      .map { UpdateNowButtonClicked }

  private fun enterDrugStockClicked() = enterDrugStockButton
      .clicks()
      .map { EnterDrugStockButtonClicked() }

  override fun openPatientSearchScreen(additionalIdentifier: Identifier?) {
    val screenKey = InstantSearchScreenKey(
        additionalIdentifier = additionalIdentifier,
        initialSearchQuery = null,
        patientPrefillInfo = null)

    router.push(screenKey)
  }

  private fun showStatus(@IdRes statusViewId: Int) {
    userStatusViewflipper.apply {
      val statusViewIndex = indexOfChildId(statusViewId)

      // Avoid duplicate calls because ViewFlipper re-plays transition
      // animations even if the child-to-display is the same.
      if (displayedChild != statusViewIndex) {
        displayedChild = statusViewIndex
      }
    }
  }

  private fun showUserAccountStatus(@IdRes statusViewId: Int) {
    showStatus(statusViewId)
    currentStatusViewId = userStatusViewflipper.currentView.id
  }

  override fun showUserStatusAsWaitingForApproval() {
    showUserAccountStatus(R.id.userStatusAwaitingApproval)
  }

  override fun renderAppUpdateReason(appStalenessInMonths: Int) {
    appUpdateCardUpdateReason.text = resources.getString(R.string.update_required_reason, appStalenessInMonths)
  }

  override fun showUserStatusAsApproved() {
    showUserAccountStatus(R.id.userStatusApproved)
  }

  override fun showUserStatusAsPendingVerification() {
    showUserAccountStatus(R.id.userAwaitingSmsVerification)
  }

  override fun hideUserAccountStatus() {
    // By changing to an empty child instead of hiding the ViewFlipper entirely,
    // ViewFlipper's change animations can be re-used for this transition.
    showUserAccountStatus(R.id.userStatusHiddenView)
  }

  override fun openEnterCodeManuallyScreen() {
    router.push(EnterOtpScreen.Key())
  }

  override fun openScanSimpleIdCardScreen() {
    router.push(ScanSimpleIdScreenKey(OpenedFrom.PatientsTabScreen))
  }

  override fun hideSyncIndicator() {
    syncIndicator.visibility = View.GONE
  }

  override fun showSyncIndicator() {
    syncIndicator.visibility = View.VISIBLE
  }

  override fun showAppUpdateDialog() {
    AppUpdateDialog.show(activity.supportFragmentManager)
  }

  override fun openPatientSummary(patientId: UUID) {
    router.push(PatientSummaryScreenKey(patientId, OpenIntention.ViewExistingPatient, Instant.now(utcClock)))
  }

  override fun openSimpleOnPlaystore() {
    val packageManager = requireContext().packageManager
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL_FOR_SIMPLE))

    if (intent.resolveActivity(packageManager) != null) {
      requireContext().startActivity(intent)
    } else {
      CrashReporter.report(ActivityNotFoundException("Unable to open play store url because no supporting apps were found."))
    }
  }

  override fun showCriticalAppUpdateDialog(appUpdateNudgePriority: AppUpdateNudgePriority) {
    router.push(CriticalAppUpdateDialog.Key(appUpdateNudgePriority))
  }

  override fun openEnterDrugStockScreen() {
    router.push(EnterDrugStockScreen.Key())
  }

  override fun showNoActiveNetworkConnectionDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.drug_stock_reminder_no_active_network_connection_dialog_title)
        .setMessage(R.string.drug_stock_reminder__no_active_network_connection_dialog_message)
        .setPositiveButton(R.string.drug_stock_reminder__no_active_network_connection_dialog_positive_button, null)
        .show()
  }

  override fun openDrugStockReportsForm() {
    router.push(MonthlyReportsScreen.Key(DrugStockReports))
  }

  private fun showHomeScreenBackground(@IdRes viewId: Int) {
    illustrationLayout.apply {
      displayedChild = indexOfChildId(viewId)
    }
    illustrationLayout.visibility = View.VISIBLE
  }

  override fun showIllustration() {
    showHomeScreenBackground(R.id.homeIllustration)
  }

  override fun showCriticalAppUpdateCard() {
    showHomeScreenBackground(R.id.appUpdateCardLayout)
  }

  override fun showDrugStockReminderCard() {
    val previousMonthAndYear = previousMonthAndYear()
    drugStockReminderCardSubTitle.text = resources.getString(R.string.drug_stock_reminder_card_content, previousMonthAndYear)

    showHomeScreenBackground(R.id.drugStockReminderCardLayout)
  }

  private fun previousMonthAndYear(): String {
    val localDate = LocalDate.now(userClock).minusMonths(1)
    return monthYearDateFormatter.format(localDate)
  }

  interface Injector {
    fun inject(target: PatientsTabScreen)
  }

  @Parcelize
  class Key : ScreenKey() {

    override val analyticsName = "Patients"

    override fun instantiateFragment() = PatientsTabScreen()
  }
}
