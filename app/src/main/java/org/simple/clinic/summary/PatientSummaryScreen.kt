package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.Init
import com.spotify.mobius.Update
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.cvdrisk.StatinInfo
import org.simple.clinic.databinding.ScreenPatientSummaryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.editpatient.EditPatientScreen
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreen
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreenExpectingResult
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patientattribute.entry.BMIEntrySheet
import org.simple.clinic.reassignpatient.ReassignPatientSheet
import org.simple.clinic.reassignpatient.ReassignPatientSheetOpenedFrom
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionScreen
import org.simple.clinic.summary.addcholesterol.CholesterolEntrySheet
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.clinicaldecisionsupport.ui.ClinicalDecisionHighBpAlert
import org.simple.clinic.summary.compose.StatinNudge
import org.simple.clinic.summary.linkId.LinkIdWithPatientSheet.LinkIdWithPatientSheetKey
import org.simple.clinic.summary.teleconsultation.contactdoctor.ContactDoctorSheet
import org.simple.clinic.summary.teleconsultation.messagebuilder.LongTeleconsultMessageBuilder_Old
import org.simple.clinic.summary.ui.PatientSummaryToolbar
import org.simple.clinic.summary.updatephone.UpdatePhoneNumberDialog
import org.simple.clinic.teleconsultlog.teleconsultrecord.screen.TeleconsultRecordScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.applyInsetsBottomPadding
import org.simple.clinic.util.messagesender.WhatsAppMessageSender
import org.simple.clinic.util.setFragmentResultListener
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.compose.PatientStatusView
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.scrollToChild
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class PatientSummaryScreen :
    BaseScreen<
        PatientSummaryScreenKey,
        ScreenPatientSummaryBinding,
        PatientSummaryModel,
        PatientSummaryEvent,
        PatientSummaryEffect,
        PatientSummaryViewEffect>(),
    PatientSummaryScreenUi,
    PatientSummaryUiActions,
    PatientSummaryChildView,
    HandlesBack {

  private val rootLayout
    get() = binding.rootLayout

  private val appbar
    get() = binding.patientSummaryAppBar

  private val drugSummaryView
    get() = binding.drugSummaryView

  private val bloodPressureSummaryView
    get() = binding.bloodPressureSummaryView

  private val bloodSugarSummaryView
    get() = binding.bloodSugarSummaryView

  private val assignedFacilityView
    get() = binding.assignedFacilityView

  private val medicalHistorySummaryView
    get() = binding.medicalHistorySummaryView

  private val summaryLoadingProgressBar
    get() = binding.summaryLoadingProgressBar

  private val summaryViewsContainer
    get() = binding.summaryViewsContainer

  private val doneButton
    get() = binding.doneButton

  private val teleconsultButton
    get() = binding.teleconsultButton

  private val logTeleconsultButton
    get() = binding.logTeleconsultButton

  private val logTeleconsultButtonFrame
    get() = binding.logTeleconsultButtonFrame

  private val facilityNameAndDateTextView
    get() = binding.facilityNameAndDateTextView

  private val labelRegistered
    get() = binding.labelRegistered

  private val doneButtonFrame
    get() = binding.doneButtonFrame

  private val nextAppointmentFacilityView
    get() = binding.nextAppointmentFacilityView

  private val composeView
    get() = binding.composeView

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var userClock: UserClock

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var effectHandlerFactory: PatientSummaryEffectHandler.Factory

  @Inject
  lateinit var longTeleconsultMessageBuilder: LongTeleconsultMessageBuilder_Old

  @Inject
  lateinit var whatsAppMessageSender: WhatsAppMessageSender

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var configReader: ConfigReader

  @Inject
  lateinit var country: Country

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val hotEvents = PublishSubject.create<PatientSummaryEvent>()
  private val hardwareBackClicks = PublishSubject.create<Unit>()
  private val subscriptions = CompositeDisposable()

  private val additionalEvents = DeferredEventSource<PatientSummaryEvent>()

  private var statinInfo by mutableStateOf(StatinInfo.default())

  private var showPatientDiedStatusView by mutableStateOf(false)

  private var showClinicalDecisionAlert by mutableStateOf(false)
  private var animateClinicalDecisionSupportAlert by mutableStateOf(false)

  override fun defaultModel(): PatientSummaryModel {
    return PatientSummaryModel.from(screenKey.intention, screenKey.patientUuid)
  }

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ): ScreenPatientSummaryBinding {
    return ScreenPatientSummaryBinding.inflate(layoutInflater, container, false)
  }

  override fun uiRenderer(): ViewRenderer<PatientSummaryModel> {
    return PatientSummaryViewRenderer(
        ui = this,
        modelUpdateCallback = { model ->
          modelUpdateCallback?.invoke(model)
        },
        userClock = userClock,
        cdssOverdueLimit = configReader.long("cdss_overdue_limit", 2).toInt()
    )
  }

  override fun viewEffectHandler() = PatientSummaryViewEffectHandler(this)

  override fun events(): Observable<PatientSummaryEvent> {
    return Observable
        .mergeArray(
            backClicks(),
            doneClicks(),
            bloodPressureSaves(),
            contactDoctorClicks(),
            hotEvents,
            logTeleconsultClicks(),
            changeAssignedFacilityClicks(),
            nextAppointmentActionClicks(),
            assignedFacilityChanges()
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun createUpdate(): Update<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect> {
    return PatientSummaryUpdate(
        isPatientReassignmentFeatureEnabled = features.isEnabled(Feature.PatientReassignment),
        isPatientStatinNudgeV1Enabled = features.isEnabled(Feature.PatientStatinNudge),
        isNonLabBasedStatinNudgeEnabled = features.isEnabled(Feature.NonLabBasedStatinNudge),
        isLabBasedStatinNudgeEnabled = features.isEnabled(Feature.LabBasedStatinNudge),
    )
  }

  override fun createInit(): Init<PatientSummaryModel, PatientSummaryEffect> {
    return PatientSummaryInit()
  }

  override fun createEffectHandler(viewEffectsConsumer: Consumer<PatientSummaryViewEffect>): ObservableTransformer<PatientSummaryEffect, PatientSummaryEvent> {
    return effectHandlerFactory.create(
        viewEffectsConsumer = viewEffectsConsumer
    ).build()
  }

  override fun additionalEventSources() = listOf(
      additionalEvents
  )

  override fun onAttach(context: Context) {
    super.onAttach(context)
    requireContext().injector<Injector>().inject(this)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    subscriptions.clear()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setFragmentResultListener(
        ScreenRequest.ScheduleAppointmentSheet,
        ScreenRequest.SelectFacility,
        ScreenRequest.ReassignPatientWarningSheet,
        ScreenRequest.BMIEntrySheet
    ) { requestKey, result ->
      if (result is Succeeded) {
        handleScreenResult(requestKey, result)
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Not sure why but the keyboard stays visible when coming from search.
    rootLayout.hideKeyboard()
    doneButtonFrame.applyInsetsBottomPadding()
    logTeleconsultButtonFrame.applyInsetsBottomPadding()

    subscriptions.add(setupChildViewVisibility())

    composeView.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        SimpleTheme {
          ClinicalDecisionHighBpAlert(
              showAlert = showClinicalDecisionAlert,
              animateExit = animateClinicalDecisionSupportAlert,
              modifier = Modifier.padding(
                  start = dimensionResource(R.dimen.spacing_8),
                  end = dimensionResource(R.dimen.spacing_8),
                  top = dimensionResource(R.dimen.spacing_8),
              ),
          )

          StatinNudge(
              statinInfo = statinInfo,
              modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
              isNonLabBasedStatinNudgeEnabled = features.isEnabled(Feature.NonLabBasedStatinNudge),
              isLabBasedStatinNudgeEnabled = features.isEnabled(Feature.LabBasedStatinNudge),
              addSmokingClick = { additionalEvents.notify(AddSmokingClicked) },
              addBMIClick = { additionalEvents.notify(AddBMIClicked) },
              addCholesterol = { additionalEvents.notify(AddCholesterolClicked) },
              useVeryHighRiskAsThreshold = country.isoCountryCode == Country.SRI_LANKA
          )

          if (showPatientDiedStatusView) {
            PatientStatusView(
                modifier = Modifier.padding(
                    start = dimensionResource(R.dimen.spacing_8),
                    end = dimensionResource(R.dimen.spacing_8),
                    top = dimensionResource(R.dimen.spacing_8),
                ),
                text = stringResource(R.string.patient_status_died),
                icon = painterResource(R.drawable.ic_patient_dead_32dp)
            )
          }
        }
      }
    }
  }

  private fun handleScreenResult(requestKey: Parcelable, result: Succeeded) {
    when (requestKey) {
      is ScreenRequest.ScheduleAppointmentSheet -> {
        val sheetOpenedFrom = ScheduleAppointmentSheet.sheetOpenedFrom(result)
        additionalEvents.notify(ScheduledAppointment(sheetOpenedFrom))
      }

      is ScreenRequest.SelectFacility -> {
        val selectedFacility = (result.result as FacilitySelectionScreen.SelectedFacility).facility
        additionalEvents.notify(NewAssignedFacilitySelected(selectedFacility))
      }

      is ScreenRequest.ReassignPatientWarningSheet -> {
        val sheetClosed = (result.result as ReassignPatientSheet.SheetClosed)
        additionalEvents.notify(PatientReassignmentWarningClosed(
            patientUuid = screenKey.patientUuid,
            screenCreatedTimestamp = screenKey.screenCreatedTimestamp,
            sheetOpenedFrom = sheetClosed.sheetOpenedFrom,
            sheetClosedFrom = sheetClosed.sheetClosedFrom
        ))
      }

      is ScreenRequest.BMIEntrySheet -> {
        additionalEvents.notify(BMIReadingAdded)
      }

      is ScreenRequest.CholesterolEntrySheet -> {
        additionalEvents.notify(CholesterolAdded)
      }
    }
  }

  @SuppressLint("CheckResult")
  private fun setupChildViewVisibility(): Disposable {
    val modelUpdates: List<Observable<PatientSummaryChildModel>> =
        listOf(
            this,
            drugSummaryView,
            bloodPressureSummaryView,
            bloodSugarSummaryView,
            assignedFacilityView,
            medicalHistorySummaryView
        ).map(::createSummaryChildModelStream)

    return Observable
        .combineLatest(modelUpdates) { models -> models.map { it as PatientSummaryChildModel } }
        .filter { models -> models.all(PatientSummaryChildModel::readyToRender) }
        .take(1)
        .subscribe {
          summaryLoadingProgressBar.visibility = GONE
          summaryViewsContainer.visibility = VISIBLE
        }
  }

  private fun createSummaryChildModelStream(
      summaryChildView: PatientSummaryChildView
  ): Observable<PatientSummaryChildModel> {
    return Observable.create { emitter ->
      summaryChildView.registerSummaryModelUpdateCallback(emitter::onNext)

      emitter.setCancellable { summaryChildView.registerSummaryModelUpdateCallback(null) }
    }
  }

  private fun createEditPatientScreenKey(
      patientSummaryProfile: PatientSummaryProfile
  ): EditPatientScreen.Key {
    return EditPatientScreen.Key(
        patientSummaryProfile.patient,
        patientSummaryProfile.address,
        patientSummaryProfile.phoneNumber,
        patientSummaryProfile.alternativeId
    )
  }

  private fun doneClicks() = doneButton
      .clicks()
      .map {
        PatientSummaryDoneClicked(screenKey.patientUuid, screenKey.screenCreatedTimestamp)
      }

  private fun contactDoctorClicks() = teleconsultButton.clicks().map { ContactDoctorClicked }

  private fun logTeleconsultClicks() = logTeleconsultButton.clicks().map { LogTeleconsultClicked }

  private fun backClicks(): Observable<UiEvent> {
    return hardwareBackClicks
        .throttleFirst(500, TimeUnit.MILLISECONDS)
        .map {
          PatientSummaryBackClicked(screenKey.patientUuid, screenKey.screenCreatedTimestamp)
        }
  }

  private fun changeAssignedFacilityClicks(): Observable<PatientSummaryEvent> {
    return Observable.create { emitter ->
      assignedFacilityView.changeAssignedFacilityClicks = { emitter.onNext(ChangeAssignedFacilityClicked) }

      emitter.setCancellable { assignedFacilityView.changeAssignedFacilityClicks = null }
    }
  }

  private fun nextAppointmentActionClicks(): Observable<PatientSummaryEvent> {
    return Observable.create { emitter ->
      nextAppointmentFacilityView.nextAppointmentActionClicks = { emitter.onNext(NextAppointmentActionClicked) }

      emitter.setCancellable { nextAppointmentFacilityView.nextAppointmentActionClicks = null }
    }
  }

  private fun assignedFacilityChanges(): Observable<AssignedFacilityChanged> {
    return Observable.create { emitter ->
      assignedFacilityView.assignedFacilityChanges = { emitter.onNext(AssignedFacilityChanged) }

      emitter.setCancellable { assignedFacilityView.assignedFacilityChanges = null }
    }
  }

  override fun onBackPressed(): Boolean {
    hardwareBackClicks.onNext(Unit)
    return true
  }

  private fun bloodPressureSaves(): Observable<PatientSummaryBloodPressureSaved> {
    return Observable.create { emitter ->
      bloodPressureSummaryView.bpRecorded = { emitter.onNext(PatientSummaryBloodPressureSaved) }

      emitter.setCancellable { bloodPressureSummaryView.bpRecorded = null }
    }
  }

  @SuppressLint("SetTextI18n")
  override fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile) {
    val patient = patientSummaryProfile.patient
    val ageValue = patient.ageDetails.estimateAge(userClock)

    displayRegistrationFacilityName(patientSummaryProfile)
  }

  override fun renderPatientSummaryToolbar(patientSummaryProfile: PatientSummaryProfile) {
    val patient = patientSummaryProfile.patient
    val ageValue = patient.ageDetails.estimateAge(userClock)

    appbar.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

      setContent {
        SimpleTheme {
          PatientSummaryToolbar(
              patientName = patient.fullName,
              gender = patient.gender,
              age = ageValue,
              address = patientSummaryProfile.address.completeAddress,
              phoneNumber = patientSummaryProfile.phoneNumber,
              bpPassport = patientSummaryProfile.bpPassport,
              alternativeId = patientSummaryProfile.alternativeId,
              onBack = {
                hotEvents.onNext(PatientSummaryBackClicked(screenKey.patientUuid, screenKey.screenCreatedTimestamp))
              },
              onContact = {
                hotEvents.onNext(ContactPatientClicked)
              },
              onEditPatient = {
                hotEvents.onNext(PatientSummaryEditClicked)
              }
          )
        }
      }
    }
  }

  private fun displayRegistrationFacilityName(patientSummaryProfile: PatientSummaryProfile) {
    val registeredFacilityName = patientSummaryProfile.facility?.name

    if (registeredFacilityName != null) {
      val recordedAt = patientSummaryProfile.patient.recordedAt
      val recordedDate = dateFormatter.format(recordedAt.toLocalDateAtZone(userClock.zone))
      val facilityNameAndDate = requireContext().getString(R.string.patientsummary_registered_facility, recordedDate, registeredFacilityName)

      facilityNameAndDateTextView.visibility = VISIBLE
      labelRegistered.visibility = VISIBLE
      facilityNameAndDateTextView.text = facilityNameAndDate
    } else {
      facilityNameAndDateTextView.visibility = GONE
      labelRegistered.visibility = GONE
    }
  }

  override fun showScheduleAppointmentSheet(
      patientUuid: UUID,
      sheetOpenedFrom: AppointmentSheetOpenedFrom,
      currentFacility: Facility
  ) {
    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = currentFacility.name,
        continuation = ContinueToScreenExpectingResult(
            requestType = ScreenRequest.ScheduleAppointmentSheet,
            screenKey = ScheduleAppointmentSheet.Key(patientUuid, sheetOpenedFrom)
        )
    ))
  }

  override fun goToPreviousScreen() {
    router.pop()
  }

  override fun goToHomeScreen() {
    router.clearHistoryAndPush(HomeScreenKey)
  }

  override fun showUpdatePhoneDialog(patientUuid: UUID) {
    router.push(UpdatePhoneNumberDialog.Key(patientUuid))
  }

  override fun showAddPhoneDialog(patientUuid: UUID) {
    router.push(AddPhoneNumberDialog.Key(patientUuid))
  }

  override fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier) {
    router.push(LinkIdWithPatientSheetKey(patientUuid = patientUuid,
        identifier = identifier,
        openedFrom = screenKey))
  }

  override fun showDiabetesView() {
    bloodSugarSummaryView.visibility = VISIBLE
  }

  override fun hideDiabetesView() {
    bloodSugarSummaryView.visibility = GONE
  }

  override fun showEditPatientScreen(
      patientSummaryProfile: PatientSummaryProfile,
      currentFacility: Facility
  ) {

    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = currentFacility.name,
        continuation = ContinueToScreen(createEditPatientScreenKey(patientSummaryProfile))
    ))
  }

  override fun showDiagnosisError() {
    // Diagnosis error message could be obscured, scroll until the entire view is visible
    summaryViewsContainer.scrollToChild(medicalHistorySummaryView) {
      medicalHistorySummaryView.showDiagnosisError()
    }
  }

  override fun openPatientContactSheet(patientUuid: UUID) {
    router.push(ContactPatientBottomSheet.Key(patientUuid))
  }

  override fun openContactDoctorSheet(patientUuid: UUID) {
    val intent = ContactDoctorSheet.intent(requireContext(), patientUuid)
    requireContext().startActivity(intent)
  }

  override fun showTeleconsultButton() {
    teleconsultButton.visibility = VISIBLE
  }

  override fun hideTeleconsultButton() {
    teleconsultButton.visibility = GONE
  }

  override fun showAssignedFacilityView() {
    assignedFacilityView.visibility = VISIBLE
  }

  override fun hideAssignedFacilityView() {
    assignedFacilityView.visibility = GONE
  }

  override fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?) {
    modelUpdateCallback = callback
  }

  override fun hideDoneButton() {
    doneButtonFrame.visibility = GONE
  }

  override fun showTeleconsultLogButton() {
    logTeleconsultButtonFrame.visibility = VISIBLE
  }

  override fun navigateToTeleconsultRecordScreen(patientUuid: UUID, teleconsultRecordId: UUID) {
    router.push(TeleconsultRecordScreenKey(patientUuid, teleconsultRecordId))
  }

  override fun showAddMeasurementsWarningDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.warning_add_measurements_title)
        .setMessage(R.string.warning_add_measurements_message)
        .setPositiveButton(R.string.warning_add_measurements_positive_button, null)
        .setNegativeButton(R.string.warning_add_measurements_negative_button) { _, _ ->
          hotEvents.onNext(MeasurementWarningNotNowClicked(
              patientUuid = screenKey.patientUuid,
              screenCreatedTimestamp = screenKey.screenCreatedTimestamp
          ))
        }
        .show()
  }

  override fun showAddBloodPressureWarningDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.warning_add_blood_pressure_title)
        .setMessage(R.string.warning_add_blood_pressure_message)
        .setPositiveButton(R.string.warning_add_blood_pressure_positive_button, null)
        .setNegativeButton(R.string.warning_add_blood_pressure_negative_button) { _, _ ->
          hotEvents.onNext(MeasurementWarningNotNowClicked(
              patientUuid = screenKey.patientUuid,
              screenCreatedTimestamp = screenKey.screenCreatedTimestamp
          ))
        }
        .show()
  }

  override fun showAddBloodSugarWarningDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.warning_add_blood_sugar_title)
        .setMessage(R.string.warning_add_blood_sugar_message)
        .setPositiveButton(R.string.warning_add_blood_sugar_positive_button, null)
        .setNegativeButton(R.string.warning_add_blood_sugar_negative_button) { _, _ ->
          hotEvents.onNext(MeasurementWarningNotNowClicked(
              patientUuid = screenKey.patientUuid,
              screenCreatedTimestamp = screenKey.screenCreatedTimestamp
          ))
        }
        .show()
  }

  override fun showDiabetesDiagnosisWarning() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.diabetes_warning_dialog_title)
        .setMessage(R.string.diabetes_warning_dialog_desc)
        .setPositiveButton(R.string.diabetes_warning_dialog_positive_button) { _, _ ->
          hotEvents.onNext(HasDiabetesClicked)
        }
        .setNegativeButton(R.string.diabetes_warning_dialog_negative_button, null)
        .show()
  }

  override fun showHypertensionDiagnosisWarning(continueToDiabetesDiagnosisWarning: Boolean) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.htn_warning_dialog_title)
        .setMessage(R.string.htn_warning_dialog_desc)
        .setPositiveButton(R.string.htn_warning_dialog_positive_button) { _, _ ->
          hotEvents.onNext(HasHypertensionClicked(continueToDiabetesDiagnosisWarning))
        }
        .setNegativeButton(R.string.htn_warning_dialog_negative_button) { _, _ ->
          hotEvents.onNext(HypertensionNotNowClicked(continueToDiabetesDiagnosisWarning))
        }
        .show()
  }

  override fun showSmokingStatusDialog() {
    val options = arrayOf(
        getString(R.string.smoking_status_dialog_option_yes),
        getString(R.string.smoking_status_dialog_option_no))
    var selectedOption = 1

    MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Simple_MaterialAlertDialog_CheckedItem)
        .setTitle(R.string.smoking_status_dialog_title)
        .setSingleChoiceItems(options, selectedOption) { _, indexSelected ->
          selectedOption = indexSelected
        }
        .setPositiveButton(R.string.smoking_status_dialog_title_positive_button) { _, _ ->
          when (selectedOption) {
            0 -> hotEvents.onNext(SmokingStatusAnswered(Answer.Yes))
            1 -> hotEvents.onNext(SmokingStatusAnswered(Answer.No))
            else -> {}
          }
        }
        .setNegativeButton(R.string.smoking_status_dialog_title_negative_button, null)
        .show()
  }

  override fun openCholesterolEntrySheet(patientUuid: UUID) {
    router.pushExpectingResult(ScreenRequest.CholesterolEntrySheet, CholesterolEntrySheet.Key(patientUuid))
  }

  override fun openBMIEntrySheet(patientUuid: UUID) {
    router.pushExpectingResult(ScreenRequest.BMIEntrySheet, BMIEntrySheet.Key(patientUuid))
  }

  override fun openSelectFacilitySheet() {
    router.pushExpectingResult(ScreenRequest.SelectFacility, FacilitySelectionScreen.Key())
  }

  override fun dispatchNewAssignedFacility(facility: Facility) {
    assignedFacilityView.onNewAssignedFacilitySelected(facility)
  }

  override fun hidePatientDiedStatus() {
    showPatientDiedStatusView = false
  }

  override fun showPatientDiedStatus() {
    showPatientDiedStatusView = true
  }

  override fun showNextAppointmentCard() {
    nextAppointmentFacilityView.visibility = VISIBLE
  }

  override fun hideNextAppointmentCard() {
    nextAppointmentFacilityView.visibility = GONE
  }

  override fun refreshNextAppointment() {
    nextAppointmentFacilityView.refreshAppointmentDetails()
  }

  override fun showClinicalDecisionSupportAlert() {
    showClinicalDecisionAlert = true
  }

  override fun hideClinicalDecisionSupportAlert() {
    showClinicalDecisionAlert = false
    animateClinicalDecisionSupportAlert = true
  }

  override fun hideClinicalDecisionSupportAlertWithoutAnimation() {
    showClinicalDecisionAlert = false
    animateClinicalDecisionSupportAlert = false
  }

  override fun updateStatinAlert(statinInfo: StatinInfo) {
    this.statinInfo = statinInfo
  }

  override fun showReassignPatientWarningSheet(
      patientUuid: UUID,
      currentFacility: Facility,
      sheetOpenedFrom: ReassignPatientSheetOpenedFrom
  ) {
    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = currentFacility.name,
        continuation = ContinueToScreenExpectingResult(
            requestType = ScreenRequest.ReassignPatientWarningSheet,
            screenKey = ReassignPatientSheet.Key(patientUuid, sheetOpenedFrom)
        )
    ))
  }

  interface Injector {
    fun inject(target: PatientSummaryScreen)
  }

  sealed class ScreenRequest : Parcelable {

    @Parcelize
    data object ScheduleAppointmentSheet : ScreenRequest()

    @Parcelize
    data object SelectFacility : ScreenRequest()

    @Parcelize
    data object ReassignPatientWarningSheet : ScreenRequest()

    @Parcelize
    data object BMIEntrySheet : ScreenRequest()

    @Parcelize
    data object CholesterolEntrySheet : ScreenRequest()
  }
}
