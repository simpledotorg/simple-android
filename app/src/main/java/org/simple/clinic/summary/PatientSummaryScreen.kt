package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannedString
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
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
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
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
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionScreen
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.linkId.LinkIdWithPatientSheet.LinkIdWithPatientSheetKey
import org.simple.clinic.summary.teleconsultation.contactdoctor.ContactDoctorSheet
import org.simple.clinic.summary.teleconsultation.messagebuilder.LongTeleconsultMessageBuilder_Old
import org.simple.clinic.summary.updatephone.UpdatePhoneNumberDialog
import org.simple.clinic.teleconsultlog.teleconsultrecord.screen.TeleconsultRecordScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.messagesender.WhatsAppMessageSender
import org.simple.clinic.util.setFragmentResultListener
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.spring
import org.simple.clinic.widgets.visibleOrGone
import java.time.format.DateTimeFormatter
import java.util.UUID
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

  private val editPatientButton
    get() = binding.editPatientButton

  private val doneButton
    get() = binding.doneButton

  private val teleconsultButton
    get() = binding.teleconsultButton

  private val logTeleconsultButton
    get() = binding.logTeleconsultButton

  private val logTeleconsultButtonFrame
    get() = binding.logTeleconsultButtonFrame

  private val backButton
    get() = binding.backButton

  private val contactTextView
    get() = binding.contactTextView

  private val facilityNameAndDateTextView
    get() = binding.facilityNameAndDateTextView

  private val labelRegistered
    get() = binding.labelRegistered

  private val addressTextView
    get() = binding.addressTextView

  private val fullNameTextView
    get() = binding.fullNameTextView

  private val bpPassportTextView
    get() = binding.bpPassportTextView

  private val alternateIdTextView
    get() = binding.alternateIdTextView

  private val doneButtonFrame
    get() = binding.doneButtonFrame

  private val patientDiedStatusView
    get() = binding.patientDiedStatusView

  private val nextAppointmentFacilityView
    get() = binding.nextAppointmentFacilityView

  private val clinicalDecisionSupportAlertView
    get() = binding.clinicalDecisionSupportBpHighAlert.rootView

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

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val hotEvents = PublishSubject.create<PatientSummaryEvent>()
  private val hardwareBackClicks = PublishSubject.create<Unit>()
  private val subscriptions = CompositeDisposable()

  private val additionalEvents = DeferredEventSource<PatientSummaryEvent>()

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
        isNextAppointmentFeatureEnabled = features.isEnabled(Feature.NextAppointment),
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
            editButtonClicks(),
            phoneNumberClicks(),
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
        isPatientReassignmentFeatureEnabled = features.isEnabled(Feature.PatientReassignment)
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
    setFragmentResultListener(ScreenRequest.ScheduleAppointmentSheet, ScreenRequest.SelectFacility) { requestKey, result ->
      if (result is Succeeded) {
        handleScreenResult(requestKey, result)
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Not sure why but the keyboard stays visible when coming from search.
    rootLayout.hideKeyboard()

    subscriptions.add(setupChildViewVisibility())
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

  private fun editButtonClicks(): Observable<UiEvent> = editPatientButton.clicks().map { PatientSummaryEditClicked }

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
    return backButton.clicks()
        .mergeWith(hardwareBackClicks)
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

  private fun phoneNumberClicks(): Observable<UiEvent> {
    return contactTextView.clicks().map { ContactPatientClicked }
  }

  @SuppressLint("SetTextI18n")
  override fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile) {
    val patient = patientSummaryProfile.patient
    val ageValue = patient.ageDetails.estimateAge(userClock)

    displayNameGenderAge(patient.fullName, patient.gender, ageValue)
    displayRegistrationFacilityName(patientSummaryProfile)
    displayPhoneNumber(patientSummaryProfile.phoneNumber)
    displayPatientAddress(patientSummaryProfile.address)
    displayBpPassport(patientSummaryProfile.bpPassport)
    displayAlternativeId(patientSummaryProfile.alternativeId, patientSummaryProfile.bpPassport != null)
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

  private fun displayPatientAddress(address: PatientAddress) {
    addressTextView.text = address.completeAddress
  }

  private fun displayPhoneNumber(phoneNumber: PatientPhoneNumber?) {
    if (phoneNumber == null) {
      contactTextView.visibility = GONE

    } else {
      contactTextView.text = phoneNumber.number
      contactTextView.visibility = VISIBLE
    }
  }

  private fun displayNameGenderAge(name: String, gender: Gender, age: Int) {
    val genderLetter = resources.getString(gender.displayLetterRes)
    fullNameTextView.text = resources.getString(R.string.patientsummary_toolbar_title, name, genderLetter, age.toString())
  }

  private fun displayBpPassport(bpPassport: BusinessId?) {
    bpPassportTextView.visibleOrGone(bpPassport != null)

    bpPassportTextView.text = when (bpPassport) {
      null -> ""
      else -> {
        val identifierNumericSpan = TextAppearanceSpan(requireContext(), R.style.TextAppearance_Simple_Body2_Numeric)
        val identifier = bpPassport.identifier
        val bpPassportLabel = identifier.displayType(resources)

        buildSpannedString {
          append("$bpPassportLabel: ")

          inSpans(identifierNumericSpan) {
            append(identifier.displayValue())
          }
        }
      }
    }
  }

  private fun displayAlternativeId(alternateId: BusinessId?, isBpPassportVisible: Boolean) {
    alternateIdTextView.visibleOrGone(alternateId != null)

    alternateIdTextView.text = when (alternateId) {
      null -> ""
      else -> generateAlternativeId(alternateId)
    }
  }

  private fun generateAlternativeId(alternateId: BusinessId): SpannedString {
    val alternateIdLabel = alternateId.identifier.displayType(resources)
    val identifierNumericSpan = TextAppearanceSpan(requireContext(), R.style.TextAppearance_Simple_Body2_Numeric)
    val identifier = alternateId.identifier

    return buildSpannedString {
      append("$alternateIdLabel: ")

      inSpans(identifierNumericSpan) {
        append(identifier.displayValue())
      }
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

  override fun showEditButton() {
    editPatientButton.visibility = VISIBLE
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

  override fun openSelectFacilitySheet() {
    router.pushExpectingResult(ScreenRequest.SelectFacility, FacilitySelectionScreen.Key())
  }

  override fun dispatchNewAssignedFacility(facility: Facility) {
    assignedFacilityView.onNewAssignedFacilitySelected(facility)
  }

  override fun hidePatientDiedStatus() {
    patientDiedStatusView.visibility = GONE
  }

  override fun showPatientDiedStatus() {
    patientDiedStatusView.visibility = VISIBLE
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
    clinicalDecisionSupportAlertView.translationY = clinicalDecisionSupportAlertView.height.unaryMinus().toFloat()

    val spring = clinicalDecisionSupportAlertView.spring(DynamicAnimation.TRANSLATION_Y)

    val transition = AutoTransition().apply {
      excludeChildren(clinicalDecisionSupportAlertView, true)
      excludeTarget(R.id.newBPItemContainer, true)
      excludeTarget(R.id.bloodSugarItemContainer, true)
      excludeTarget(R.id.drugsSummaryContainer, true)
      // We are doing this to wait for the router transitions to be done before we start this.
      startDelay = 500
    }
    val transitionListener = object : Transition.TransitionListener {
      override fun onTransitionStart(transition: Transition) {
      }

      override fun onTransitionEnd(transition: Transition) {
        transition.removeListener(this)
        spring.animateToFinalPosition(0f)
      }

      override fun onTransitionCancel(transition: Transition) {
      }

      override fun onTransitionPause(transition: Transition) {
      }

      override fun onTransitionResume(transition: Transition) {
      }
    }
    transition.addListener(transitionListener)
    TransitionManager.beginDelayedTransition(summaryViewsContainer, transition)

    clinicalDecisionSupportAlertView.visibility = VISIBLE
  }

  override fun hideClinicalDecisionSupportAlert() {
    if (clinicalDecisionSupportAlertView.visibility != VISIBLE) return

    val spring = clinicalDecisionSupportAlertView.spring(DynamicAnimation.TRANSLATION_Y)
    (clinicalDecisionSupportAlertView.getTag(R.id.tag_clinical_decision_pending_end_listener) as?
        DynamicAnimation.OnAnimationEndListener)?.let {
      spring.removeEndListener(it)
    }

    val listener = object : DynamicAnimation.OnAnimationEndListener {
      override fun onAnimationEnd(animation: DynamicAnimation<*>?, canceled: Boolean, value: Float, velocity: Float) {
        spring.removeEndListener(this)
        clinicalDecisionSupportAlertView.visibility = GONE
      }
    }
    spring.addEndListener(listener)
    clinicalDecisionSupportAlertView.setTag(R.id.tag_clinical_decision_pending_end_listener, listener)

    val transition = AutoTransition().apply {
      excludeChildren(clinicalDecisionSupportAlertView, true)
      excludeTarget(R.id.newBPItemContainer, true)
      excludeTarget(R.id.bloodSugarItemContainer, true)
      excludeTarget(R.id.drugsSummaryContainer, true)
    }
    TransitionManager.beginDelayedTransition(summaryViewsContainer, transition)

    spring.animateToFinalPosition(clinicalDecisionSupportAlertView.height.unaryMinus().toFloat())
  }

  override fun hideClinicalDecisionSupportAlertWithoutAnimation() {
    clinicalDecisionSupportAlertView.visibility = GONE
  }

  override fun showReassignPatientWarningSheet(patientUuid: UUID) {
    // TODO: Show patient reassignment sheet
  }

  interface Injector {
    fun inject(target: PatientSummaryScreen)
  }

  sealed class ScreenRequest : Parcelable {

    @Parcelize
    object ScheduleAppointmentSheet : ScreenRequest()

    @Parcelize
    object SelectFacility : ScreenRequest()
  }
}
