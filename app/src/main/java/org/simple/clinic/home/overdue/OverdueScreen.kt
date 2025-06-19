package org.simple.clinic.home.overdue

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.f2prateek.rx.preferences2.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spotify.mobius.Update
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.permissions.RequestPermissions
import org.simple.clinic.activity.permissions.RuntimePermissions
import org.simple.clinic.appconfig.Country
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.databinding.ScreenOverdueBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature.OverdueInstantSearch
import org.simple.clinic.feature.Feature.PatientReassignment
import org.simple.clinic.feature.Features
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.home.overdue.compose.OverdueScreenView
import org.simple.clinic.home.overdue.compose.OverdueUiModel
import org.simple.clinic.home.overdue.compose.OverdueUiModelMapper
import org.simple.clinic.home.overdue.search.OverdueSearchScreen
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.overdue.PendingAppointmentsConfig
import org.simple.clinic.overdue.download.formatdialog.Download
import org.simple.clinic.overdue.download.formatdialog.SelectOverdueDownloadFormatDialog
import org.simple.clinic.overdue.download.formatdialog.Share
import org.simple.clinic.overdue.download.formatdialog.SharingInProgress
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.util.RuntimeNetworkStatus
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class OverdueScreen : BaseScreen<
    OverdueScreen.Key,
    ScreenOverdueBinding,
    OverdueModel,
    OverdueEvent,
    OverdueEffect,
    OverdueViewEffect>(), OverdueUiActions, OverdueUi {

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var country: Country

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var effectHandlerFactory: OverdueEffectHandler.Factory

  @Inject
  lateinit var lastSyncedState: Preference<LastSyncedState>

  @Inject
  lateinit var runtimeNetworkStatus: RuntimeNetworkStatus<UiEvent>

  @Inject
  lateinit var pendingAppointmentsConfig: PendingAppointmentsConfig

  @Inject
  lateinit var locale: Locale

  private val disposable = CompositeDisposable()

  private val composeView
    get() = binding.composeView

  private val isOverdueListDownloadAndShareEnabled by unsafeLazy {
    country.isoCountryCode == Country.INDIA
  }

  private var uiModelsState by mutableStateOf<List<OverdueUiModel>>(emptyList())
  private var showLoader by mutableStateOf(false)
  private var showEmptyListView by mutableStateOf(false)
  private var showAppointmentSections by mutableStateOf(false)
  private var showDownloadAndShareButton by mutableStateOf(false)
  private var showSelectedOverdueCountView by mutableStateOf(false)
  private var selectedOverdueCount by mutableIntStateOf(0)

  override fun defaultModel() = OverdueModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenOverdueBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = OverdueUiRenderer(ui = this)

  private val composeUiEvents = PublishSubject.create<OverdueEvent>()

  override fun events() = Observable.mergeArray(
      composeUiEvents,
  )
      .compose(RequestPermissions(runtimePermissions, screenResults.streamResults().ofType()))
      //      .compose(runtimeNetworkStatus::apply)
      .compose(ReportAnalyticsEvents())
      .share()
      .cast<OverdueEvent>()

  override fun createUpdate(): Update<OverdueModel, OverdueEvent, OverdueEffect> {
    val date = LocalDate.now(userClock)
    val canGeneratePdf = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    return OverdueUpdate(
        date = date,
        canGeneratePdf = canGeneratePdf
    )
  }

  override fun createInit() = OverdueInit()

  override fun createEffectHandler(
      viewEffectsConsumer: Consumer<OverdueViewEffect>
  ) = effectHandlerFactory.create(viewEffectsConsumer).build()

  override fun viewEffectHandler() = OverdueViewEffectHandler(this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    composeView.apply {
      setViewCompositionStrategy(
          ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
      )
      setContent {
        OverdueScreenView(
            showDownloadAndShareButton = showDownloadAndShareButton,
            showSelectedOverdueCountView = showSelectedOverdueCountView,
            showEmptyListView = showEmptyListView,
            showLoader = showLoader,
            showAppointmentSections = showAppointmentSections,
            selectedOverdueCount = selectedOverdueCount,
            uiModels = uiModelsState,
            onCall = { composeUiEvents.onNext(CallPatientClicked(it)) },
            onOpen = { composeUiEvents.onNext(OverduePatientClicked(it)) },
            onToggleSelection = { composeUiEvents.onNext(OverdueAppointmentCheckBoxClicked(it)) },
            onSearch = { composeUiEvents.onNext(OverdueSearchButtonClicked) },
            onToggleSection = { composeUiEvents.onNext(ChevronClicked(it)) },
            onToggleFooter = { composeUiEvents.onNext(PendingListFooterClicked) },
            onClearSelected = { composeUiEvents.onNext(ClearSelectedOverdueAppointmentsClicked) },
            onDownload = { composeUiEvents.onNext(DownloadOverdueListClicked()) },
            onShare = { composeUiEvents.onNext(ShareOverdueListClicked()) }
        )
      }
    }
  }


  override fun onDestroyView() {
    disposable.clear()
    super.onDestroyView()
  }

  override fun openPhoneMaskBottomSheet(patientUuid: UUID) {
    router.push(ContactPatientBottomSheet.Key(patientUuid))
  }

  override fun showNoActiveNetworkConnectionDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.overdue_download_no_active_network_connection_dialog_title)
        .setMessage(R.string.overdue_download_no_active_network_connection_dialog_message)
        .setPositiveButton(R.string.overdue_download_no_active_network_connection_dialog_positive_button, null)
        .show()
  }

  override fun openPatientSummary(patientUuid: UUID) {
    router.push(
        PatientSummaryScreenKey(
            patientUuid = patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        )
    )
  }

  override fun openSelectDownloadFormatDialog() {
    router.push(SelectOverdueDownloadFormatDialog.Key(Download))
  }

  override fun openSelectShareFormatDialog() {
    router.push(SelectOverdueDownloadFormatDialog.Key(Share))
  }

  override fun openProgressForSharingDialog() {
    router.push(SelectOverdueDownloadFormatDialog.Key(SharingInProgress))
  }

  override fun showOverdueAppointments(
      overdueAppointmentSections: OverdueAppointmentSections,
      selectedOverdueAppointments: Set<UUID>,
      overdueListSectionStates: OverdueListSectionStates
  ) {
    uiModelsState = OverdueUiModelMapper.from(
        overdueAppointmentSections = overdueAppointmentSections,
        clock = userClock,
        pendingListDefaultStateSize = pendingAppointmentsConfig.pendingListDefaultStateSize,
        overdueListSectionStates = overdueListSectionStates,
        isOverdueInstantSearchEnabled = features.isEnabled(OverdueInstantSearch),
        isOverdueSelectAndDownloadEnabled = country.isoCountryCode == Country.INDIA,
        selectedOverdueAppointments = selectedOverdueAppointments,
        isPatientReassignmentFeatureEnabled = features.isEnabled(PatientReassignment),
        locale = locale,
    )

    if (isOverdueListDownloadAndShareEnabled) {
      showDownloadAndShareButton = true
    }
  }

  override fun showOverdueCount(count: Int) {
    (parentFragment as HomeScreen).overdueListCountUpdated(count)
  }

  override fun showSelectedOverdueAppointmentCount(selectedOverdueAppointments: Int) {
    showSelectedOverdueCountView = true
    selectedOverdueCount = selectedOverdueAppointments
  }

  override fun hideSelectedOverdueAppointmentCount() {
    showSelectedOverdueCountView = false
  }

  override fun showProgress() {
    showLoader = true
  }

  override fun hideProgress() {
    showLoader = false
  }

  override fun showNoOverduePatientsView() {
    showEmptyListView = true
    if (isOverdueListDownloadAndShareEnabled) {
      showDownloadAndShareButton = false
    }
  }

  override fun hideNoOverduePatientsView() {
    showEmptyListView = false
  }

  override fun showOverdueAppointmentSections() {
    showAppointmentSections = true
  }

  override fun hideOverdueAppointmentSections() {
    showAppointmentSections = false
  }

  override fun openOverdueSearch() {
    router.push(OverdueSearchScreen.Key())
  }

  interface Injector {
    fun inject(target: OverdueScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Overdue"
  ) : ScreenKey() {

    override fun instantiateFragment() = OverdueScreen()
  }
}
