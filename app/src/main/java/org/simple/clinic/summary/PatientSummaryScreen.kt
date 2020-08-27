package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.screen_patient_summary.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.editpatient.EditPatientScreenKey
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation
import org.simple.clinic.facility.alertchange.Continuation.ContinueToActivity
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreen
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.RouterDirection.BACKWARD
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.summary.OpenIntention.ViewExistingPatientWithTeleconsultLog
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.linkId.LinkIdWithPatientCancelled
import org.simple.clinic.summary.linkId.LinkIdWithPatientLinked
import org.simple.clinic.summary.linkId.LinkIdWithPatientViewShown
import org.simple.clinic.summary.teleconsultation.api.TeleconsultPhoneNumber
import org.simple.clinic.summary.teleconsultation.contactdoctor.ContactDoctorSheet_Old
import org.simple.clinic.summary.teleconsultation.messagebuilder.LongTeleconsultMessageBuilder
import org.simple.clinic.summary.updatephone.UpdatePhoneNumberDialog
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.util.messagesender.WhatsAppMessageSender
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.isVisible
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.visibleOrGone
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class PatientSummaryScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), PatientSummaryScreenUi, PatientSummaryUiActions, PatientSummaryChildView {

  @Inject
  lateinit var screenRouter: ScreenRouter

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
  lateinit var longTeleconsultMessageBuilder: LongTeleconsultMessageBuilder

  @Inject
  lateinit var whatsAppMessageSender: WhatsAppMessageSender

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val snackbarActionClicks = PublishSubject.create<PatientSummaryEvent>()
  private val events: Observable<PatientSummaryEvent> by unsafeLazy {
    Observable
        .mergeArray(
            backClicks(),
            doneClicks(),
            bloodPressureSaves(),
            appointmentScheduleSheetClosed(),
            identifierLinkedEvents(),
            identifierLinkCancelledEvents(),
            editButtonClicks(),
            phoneNumberClicks(),
            contactDoctorClicks(),
            snackbarActionClicks,
            teleconsultPhoneNumberSelected()
        )
        .compose(ReportAnalyticsEvents())
        .cast<PatientSummaryEvent>()
  }

  private val viewRenderer = PatientSummaryViewRenderer(this)

  private val screenKey: PatientSummaryScreenKey by unsafeLazy {
    screenRouter.key<PatientSummaryScreenKey>(this)
  }

  private val mobiusDelegate: MobiusDelegate<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect> by unsafeLazy {
    MobiusDelegate.forView(
        events = events,
        defaultModel = PatientSummaryModel.from(screenKey.intention, screenKey.patientUuid),
        init = PatientSummaryInit(),
        update = PatientSummaryUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = { model ->
          modelUpdateCallback?.invoke(model)
          viewRenderer.render(model)
        }
    )
  }
  private val teleconsultationErrorSnackbar by unsafeLazy {
    Snackbar.make(rootLayout, R.string.patientsummary_teleconsult_network_error, Snackbar.LENGTH_INDEFINITE)
        .setAction(R.string.patientsummary_teleconsult_network_error_retry) {
          postDelayed({
            snackbarActionClicks.onNext(RetryFetchTeleconsultInfo)
          }, 100)
        }
        .setActionTextColor(ContextCompat.getColor(context, R.color.green2))
        .setAnchorView(R.id.buttonFrame)
  }

  override fun onSaveInstanceState(): Parcelable {
    return mobiusDelegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    super.onRestoreInstanceState(mobiusDelegate.onRestoreInstanceState(state))
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    // Not sure why but the keyboard stays visible when coming from search.
    rootLayout.hideKeyboard()

    val screenDestroys: Observable<ScreenDestroyed> = detaches().map { ScreenDestroyed() }
    alertFacilityChangeSheetClosed(screenDestroys)
    setupChildViewVisibility(screenDestroys)

    when (screenKey.intention) {
      is ViewExistingPatientWithTeleconsultLog -> {
        doctorButton.setButtonState(Enabled)
        doctorButton.icon = null
        doctorButton.text = context.getString(R.string.patientsummary_log_teleconsult)
        doneButton.visibility = View.GONE
        buttonFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.green3))
      }
    }
  }

  @SuppressLint("CheckResult")
  private fun setupChildViewVisibility(
      screenDestroys: Observable<ScreenDestroyed>
  ) {
    val modelUpdates: List<Observable<PatientSummaryChildModel>> =
        listOf(
            this,
            drugSummaryView,
            bloodPressureSummaryView,
            bloodSugarSummaryView,
            assignedFacilityView,
            medicalHistorySummaryView
        ).map(::createSummaryChildModelStream)

    Observable
        .combineLatest(modelUpdates) { models -> models.map { it as PatientSummaryChildModel } }
        .filter { models -> models.all(PatientSummaryChildModel::readyToRender) }
        .take(1)
        .takeUntil(screenDestroys)
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

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mobiusDelegate.start()
  }

  override fun onDetachedFromWindow() {
    if (teleconsultationErrorSnackbar.isShownOrQueued) {
      teleconsultationErrorSnackbar.dismiss()
    }
    mobiusDelegate.stop()
    super.onDetachedFromWindow()
  }

  private fun teleconsultPhoneNumberSelected(): Observable<PatientSummaryEvent> {
    return screenRouter
        .streamScreenResults()
        .ofType<ActivityResult>()
        .extractSuccessful(CONTACT_DOCTOR_SHEET) { intent ->
          val teleconsultPhoneNumberString = ContactDoctorSheet_Old.readPhoneNumberExtra(intent)
          val teleconsultPhoneNumber = TeleconsultPhoneNumber(teleconsultPhoneNumberString)

          ContactDoctorPhoneNumberSelected(teleconsultPhoneNumber)
        }
  }

  private fun editButtonClicks(): Observable<UiEvent> = editPatientButton.clicks().map { PatientSummaryEditClicked }

  private fun createEditPatientScreenKey(
      patientSummaryProfile: PatientSummaryProfile
  ): EditPatientScreenKey {
    return EditPatientScreenKey.fromPatientData(
        patientSummaryProfile.patient,
        patientSummaryProfile.address,
        patientSummaryProfile.phoneNumber,
        patientSummaryProfile.alternativeId
    )
  }

  private fun doneClicks() = doneButton.clicks().map { PatientSummaryDoneClicked(screenKey.patientUuid) }

  private fun contactDoctorClicks() = doctorButton.clicks().map { ContactDoctorClicked }

  private fun backClicks(): Observable<UiEvent> {
    val hardwareBackKeyClicks = Observable.create<Unit> { emitter ->
      val interceptor = object : BackPressInterceptor {
        override fun onInterceptBackPress(callback: BackPressInterceptCallback) {
          emitter.onNext(Unit)
          callback.markBackPressIntercepted()
        }
      }
      emitter.setCancellable { screenRouter.unregisterBackPressInterceptor(interceptor) }
      screenRouter.registerBackPressInterceptor(interceptor)
    }

    return backButton.clicks()
        .mergeWith(hardwareBackKeyClicks)
        .map {
          if (linkIdWithPatientView.isVisible) {
            PatientSummaryLinkIdCancelled
          } else {
            PatientSummaryBackClicked(screenKey.patientUuid, screenKey.screenCreatedTimestamp)
          }
        }
  }

  private fun bloodPressureSaves(): Observable<PatientSummaryBloodPressureSaved> {
    return Observable.create { emitter ->
      bloodPressureSummaryView.bpRecorded = { emitter.onNext(PatientSummaryBloodPressureSaved) }

      emitter.setCancellable { bloodPressureSummaryView.bpRecorded = null }
    }
  }

  private fun appointmentScheduleSheetClosed() = screenRouter.streamScreenResults()
      .ofType<ActivityResult>()
      .extractSuccessful(SUMMARY_REQCODE_SCHEDULE_APPOINTMENT) { intent ->
        ScheduleAppointmentSheet.readExtra<ScheduleAppointmentSheetExtra>(intent)
      }
      .map { ScheduledAppointment(it.sheetOpenedFrom) }

  @SuppressLint("CheckResult")
  private fun alertFacilityChangeSheetClosed(onDestroys: Observable<ScreenDestroyed>) {
    screenRouter.streamScreenResults()
        .ofType<ActivityResult>()
        .extractSuccessful(SUMMARY_REQCODE_ALERT_FACILITY_CHANGE) { intent ->
          AlertFacilityChangeSheet.readContinuationExtra<Continuation>(intent)
        }
        .takeUntil(onDestroys)
        .subscribe(::openContinuation)
  }

  private fun openContinuation(continuation: Continuation) {
    when (continuation) {
      is ContinueToScreen -> screenRouter.push(continuation.screenKey)
      is ContinueToActivity -> activity.startActivityForResult(continuation.intent, continuation.requestCode)
    }
  }

  private fun identifierLinkedEvents(): Observable<UiEvent> {
    return linkIdWithPatientView
        .uiEvents()
        .ofType<LinkIdWithPatientLinked>()
        .map { PatientSummaryLinkIdCompleted }
  }

  private fun identifierLinkCancelledEvents(): Observable<UiEvent> {
    return linkIdWithPatientView
        .uiEvents()
        .ofType<LinkIdWithPatientCancelled>()
        .map { PatientSummaryLinkIdCancelled }
  }

  private fun phoneNumberClicks(): Observable<UiEvent> {
    return contactTextView.clicks().map { ContactPatientClicked }
  }

  @SuppressLint("SetTextI18n")
  override fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile) {
    val patient = patientSummaryProfile.patient
    val ageValue = DateOfBirth.fromPatient(patient, userClock).estimateAge(userClock)

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
      val facilityNameAndDate = context.getString(R.string.patientsummary_registered_facility, recordedDate, registeredFacilityName)

      facilityNameAndDateTextView.visibility = View.VISIBLE
      labelRegistered.visibility = View.VISIBLE
      facilityNameAndDateTextView.text = facilityNameAndDate
    } else {
      facilityNameAndDateTextView.visibility = View.GONE
      labelRegistered.visibility = View.GONE
    }
  }

  private fun displayPatientAddress(address: PatientAddress) {
    val addressFields = listOf(
        address.streetAddress,
        address.colonyOrVillage,
        address.district,
        address.state,
        address.zone
    ).filterNot { it.isNullOrBlank() }

    addressTextView.text = addressFields.joinToString()
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
        val identifier = bpPassport.identifier
        val numericSpan = TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Numeric_White72)
        Truss()
            .append(identifier.displayType(resources))
            .append(": ")
            .pushSpan(numericSpan)
            .append(identifier.displayValue())
            .popSpan()
            .build()
      }
    }
  }

  private fun displayAlternativeId(bangladeshNationalId: BusinessId?, isBpPassportVisible: Boolean) {
    bangladeshNationalIdTextView.visibleOrGone(bangladeshNationalId != null)

    bangladeshNationalIdTextView.text = when (bangladeshNationalId) {
      null -> ""
      else -> {
        val identifier = bangladeshNationalId.identifier
        val numericSpan = TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Numeric_White72)

        val formattedIdentifier = Truss()
            .append(context.getString(R.string.patientsummary_bangladesh_national_id))
            .append(": ")
            .pushSpan(numericSpan)
            .append(identifier.displayValue())
            .popSpan()
            .build()

        if (isBpPassportVisible) "${Unicode.bullet} $formattedIdentifier" else formattedIdentifier
      }
    }
  }

  override fun showScheduleAppointmentSheet(
      patientUuid: UUID,
      sheetOpenedFrom: AppointmentSheetOpenedFrom,
      currentFacility: Facility
  ) {
    val scheduleAppointmentIntent = ScheduleAppointmentSheet.intent(context, patientUuid, ScheduleAppointmentSheetExtra(sheetOpenedFrom))
    val alertFacilityChangeIntent = AlertFacilityChangeSheet.intent(
        context,
        currentFacility.name,
        ContinueToActivity(scheduleAppointmentIntent, SUMMARY_REQCODE_SCHEDULE_APPOINTMENT)
    )

    activity.startActivityForResult(alertFacilityChangeIntent, SUMMARY_REQCODE_ALERT_FACILITY_CHANGE)
  }

  override fun goToPreviousScreen() {
    screenRouter.pop()
  }

  override fun goToHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey, direction = BACKWARD)
  }

  override fun showUpdatePhoneDialog(patientUuid: UUID) {
    UpdatePhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
  }

  override fun showAddPhoneDialog(patientUuid: UUID) {
    AddPhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
  }

  override fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier) {
    linkIdWithPatientView.downstreamUiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))
    linkIdWithPatientView.show { linkIdWithPatientView.visibility = View.VISIBLE }
  }

  override fun hideLinkIdWithPatientView() {
    linkIdWithPatientView.hide { linkIdWithPatientView.visibility = View.GONE }
  }

  override fun showEditButton() {
    editPatientButton.visibility = View.VISIBLE
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
    val intentForAlertSheet = AlertFacilityChangeSheet.intent(
        context,
        currentFacility.name,
        ContinueToScreen(createEditPatientScreenKey(patientSummaryProfile))
    )
    activity.startActivityForResult(intentForAlertSheet, SUMMARY_REQCODE_ALERT_FACILITY_CHANGE)
  }

  override fun showDiagnosisError() {
    // Diagnosis error message could be obscured, scroll until the entire view is visible
    summaryViewsContainer.scrollToChild(medicalHistorySummaryView) {
      medicalHistorySummaryView.showDiagnosisError()
    }
  }

  override fun openPatientContactSheet(patientUuid: UUID) {
    activity.startActivity(ContactPatientBottomSheet.intent(activity, patientUuid))
  }

  override fun contactDoctor(patientTeleconsultationInfo: PatientTeleconsultationInfo, teleconsultationPhoneNumber: String) {
    val message = longTeleconsultMessageBuilder.message(patientTeleconsultationInfo)
    whatsAppMessageSender.send(teleconsultationPhoneNumber, message)
  }

  override fun openContactDoctorSheet(
      facility: Facility,
      phoneNumbers: List<TeleconsultPhoneNumber>
  ) {
    val intent = ContactDoctorSheet_Old.intent(context, facility, phoneNumbers)
    activity.startActivityForResult(intent, CONTACT_DOCTOR_SHEET)
  }

  override fun enableContactDoctorButton() {
    doctorButton.setButtonState(Enabled)
  }

  override fun disableContactDoctorButton() {
    doctorButton.setButtonState(ButtonState.Disabled)
  }

  override fun fetchingTeleconsultInfo() {
    doctorButton.setButtonState(ButtonState.InProgress)
  }

  override fun showTeleconsultInfoError() {
    if (teleconsultationErrorSnackbar.isShown.not()) {
      teleconsultationErrorSnackbar.show()
    }
  }

  override fun showContactDoctorButton() {
    doctorButton.visibility = View.VISIBLE
  }

  override fun hideContactDoctorButton() {
    doctorButton.visibility = View.GONE
  }

  override fun showAssignedFacilityView() {
    assignedFacilityView.visibility = View.VISIBLE
  }

  override fun hideAssignedFacilityView() {
    assignedFacilityView.visibility = View.GONE
  }

  override fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?) {
    modelUpdateCallback = callback
  }
}

@Parcelize
private data class ScheduleAppointmentSheetExtra(
    val sheetOpenedFrom: AppointmentSheetOpenedFrom
) : Parcelable
