package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
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
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.linkId.LinkIdWithPatientCancelled
import org.simple.clinic.summary.linkId.LinkIdWithPatientLinked
import org.simple.clinic.summary.linkId.LinkIdWithPatientViewShown
import org.simple.clinic.summary.updatephone.UpdatePhoneNumberDialog
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.isVisible
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID
import javax.inject.Inject

class PatientSummaryScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), PatientSummaryScreenUi, PatientSummaryUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var effectHandlerFactory: PatientSummaryEffectHandler.Factory

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
            contactDoctorClicks()
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
        modelUpdateListener = viewRenderer::render
    )
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
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mobiusDelegate.start()
  }

  override fun onDetachedFromWindow() {
    mobiusDelegate.stop()
    super.onDetachedFromWindow()
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

  private fun contactDoctorClicks() = contactDoctorButton.clicks().map { ContactDoctorClicked }

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
      .filter { it.requestCode == SUMMARY_REQCODE_SCHEDULE_APPOINTMENT && it.succeeded() }
      .map { ScheduleAppointmentSheet.readExtra<ScheduleAppointmentSheetExtra>(it.data!!) }
      .map { ScheduledAppointment(it.sheetOpenedFrom) }

  @SuppressLint("CheckResult")
  private fun alertFacilityChangeSheetClosed(onDestroys: Observable<ScreenDestroyed>) {
    screenRouter.streamScreenResults()
        .ofType<ActivityResult>()
        .filter { it.requestCode == SUMMARY_REQCODE_ALERT_FACILITY_CHANGE && it.succeeded() }
        .map { AlertFacilityChangeSheet.readContinuationExtra<Continuation>(it.data!!) }
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
    displayPhoneNumber(patientSummaryProfile.phoneNumber)
    displayPatientAddress(patientSummaryProfile.address)
    displayBpPassport(patientSummaryProfile.bpPassport)
    displayAlternativeId(patientSummaryProfile.alternativeId, patientSummaryProfile.bpPassport != null)
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
    screenRouter.clearHistoryAndPush(HomeScreenKey(), direction = BACKWARD)
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
    // TODO (SM): Build WhatsApp message and start implicit intent.
  }

  override fun showContactDoctorButtonTextAndIcon() {
    contactDoctorButton.text = context.getString(R.string.patientsummary_contact_doctor)
    contactDoctorButton.setIconResource(R.drawable.ic_whatsapp)
    contactDoctorProgress.visibility = View.GONE
  }

  override fun enableContactDoctorButton() {
    contactDoctorButton.isEnabled = true
  }

  override fun disableContactDoctorButton() {
    contactDoctorButton.isEnabled = false
  }
}

@Parcelize
private data class ScheduleAppointmentSheetExtra(
    val sheetOpenedFrom: AppointmentSheetOpenedFrom
) : Parcelable
