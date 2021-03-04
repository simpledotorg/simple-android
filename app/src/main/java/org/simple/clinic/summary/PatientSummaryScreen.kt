package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannedString
import android.text.style.BulletSpan
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.Init
import com.spotify.mobius.Update
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.databinding.ScreenPatientSummaryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.editpatient.EditPatientScreenKey
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation.ContinueToActivity
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreen_Old
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.linkId.LinkIdWithPatientSheet.LinkIdWithPatientSheetKey
import org.simple.clinic.summary.teleconsultation.contactdoctor.ContactDoctorSheet
import org.simple.clinic.summary.teleconsultation.messagebuilder.LongTeleconsultMessageBuilder_Old
import org.simple.clinic.summary.updatephone.UpdatePhoneNumberDialog
import org.simple.clinic.teleconsultlog.teleconsultrecord.screen.TeleconsultRecordScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.util.messagesender.WhatsAppMessageSender
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.scrollToChild
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
        PatientSummaryEffect>(),
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

  private val bangladeshNationalIdTextView
    get() = binding.bangladeshNationalIdTextView

  private val doneButtonFrame
    get() = binding.doneButtonFrame

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

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

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val snackbarActionClicks = PublishSubject.create<PatientSummaryEvent>()
  private val hardwareBackClicks = PublishSubject.create<Unit>()
  private val subscriptions = CompositeDisposable()

  private val appointmentScheduleSheetClosed = DeferredEventSource<PatientSummaryEvent>()

  override fun defaultModel(): PatientSummaryModel {
    return PatientSummaryModel.from(screenKey.intention, screenKey.patientUuid)
  }

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?): ScreenPatientSummaryBinding {
    return ScreenPatientSummaryBinding.inflate(layoutInflater, container, false)
  }

  override fun uiRenderer(): ViewRenderer<PatientSummaryModel> {
    return PatientSummaryViewRenderer(ui = this) { model ->
      modelUpdateCallback?.invoke(model)
    }
  }

  override fun events(): Observable<PatientSummaryEvent> {
    return Observable
        .mergeArray(
            backClicks(),
            doneClicks(),
            bloodPressureSaves(),
            editButtonClicks(),
            phoneNumberClicks(),
            contactDoctorClicks(),
            snackbarActionClicks,
            logTeleconsultClicks()
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun createUpdate(): Update<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect> {
    return PatientSummaryUpdate()
  }

  override fun createInit(): Init<PatientSummaryModel, PatientSummaryEffect> {
    return PatientSummaryInit()
  }

  override fun createEffectHandler(): ObservableTransformer<PatientSummaryEffect, PatientSummaryEvent> {
    return effectHandlerFactory.create(this).build()
  }

  override fun additionalEventSources() = listOf(
      appointmentScheduleSheetClosed
  )

  override fun onAttach(context: Context) {
    super.onAttach(context)
    requireContext().injector<Injector>().inject(this)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    subscriptions.clear()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Not sure why but the keyboard stays visible when coming from search.
    rootLayout.hideKeyboard()

    subscriptions.addAll(
        setupChildViewVisibility(),
        appointmentScheduleSheetClosed()
    )
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
  ): EditPatientScreenKey {
    return EditPatientScreenKey.fromPatientData(
        patientSummaryProfile.patient,
        patientSummaryProfile.address,
        patientSummaryProfile.phoneNumber,
        patientSummaryProfile.alternativeId
    )
  }

  private fun doneClicks() = doneButton.clicks().map { PatientSummaryDoneClicked(screenKey.patientUuid) }

  private fun contactDoctorClicks() = teleconsultButton.clicks().map { ContactDoctorClicked }

  private fun logTeleconsultClicks() = logTeleconsultButton.clicks().map { LogTeleconsultClicked }

  private fun backClicks(): Observable<UiEvent> {
    return backButton.clicks()
        .mergeWith(hardwareBackClicks)
        .map {
          PatientSummaryBackClicked(screenKey.patientUuid, screenKey.screenCreatedTimestamp)
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

  private fun appointmentScheduleSheetClosed() = screenResults
      .streamResults()
      .ofType<ActivityResult>()
      .extractSuccessful(SUMMARY_REQCODE_SCHEDULE_APPOINTMENT) { intent ->
        ScheduleAppointmentSheet.readExtra<ScheduleAppointmentSheetExtra>(intent)
      }
      .subscribe {
        appointmentScheduleSheetClosed.notify(ScheduledAppointment(it!!.sheetOpenedFrom))
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
      val facilityNameAndDate = requireContext().getString(R.string.patientsummary_registered_facility, recordedDate, registeredFacilityName)

      facilityNameAndDateTextView.visibility = View.VISIBLE
      labelRegistered.visibility = View.VISIBLE
      facilityNameAndDateTextView.text = facilityNameAndDate
    } else {
      facilityNameAndDateTextView.visibility = View.GONE
      labelRegistered.visibility = View.GONE
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

  private fun displayAlternativeId(bangladeshNationalId: BusinessId?, isBpPassportVisible: Boolean) {
    bangladeshNationalIdTextView.visibleOrGone(bangladeshNationalId != null)

    bangladeshNationalIdTextView.text = when (bangladeshNationalId) {
      null -> ""
      else -> generateAlternativeId(bangladeshNationalId, isBpPassportVisible)
    }
  }

  private fun generateAlternativeId(bangladeshNationalId: BusinessId, isBpPassportVisible: Boolean): SpannedString {
    val bangladeshNationalIdLabel = requireContext().getString(R.string.patientsummary_bangladesh_national_id)
    val identifierNumericSpan = TextAppearanceSpan(requireContext(), R.style.TextAppearance_Simple_Body2_Numeric)
    val identifier = bangladeshNationalId.identifier

    return buildSpannedString {
      if (isBpPassportVisible) {
        inSpans(BulletSpan(16)) {
          append("$bangladeshNationalIdLabel: ")
        }
      } else {
        append("$bangladeshNationalIdLabel: ")
      }

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
    val scheduleAppointmentIntent = ScheduleAppointmentSheet.intent(requireContext(), patientUuid, ScheduleAppointmentSheetExtra(sheetOpenedFrom))

    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = currentFacility.name,
        continuation = ContinueToActivity(scheduleAppointmentIntent, SUMMARY_REQCODE_SCHEDULE_APPOINTMENT)
    ))
  }

  override fun goToPreviousScreen() {
    router.pop()
  }

  override fun goToHomeScreen() {
    router.clearHistoryAndPush(HomeScreenKey)
  }

  override fun showUpdatePhoneDialog(patientUuid: UUID) {
    UpdatePhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
  }

  override fun showAddPhoneDialog(patientUuid: UUID) {
    AddPhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
  }

  override fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier) {
    router.push(LinkIdWithPatientSheetKey(patientUuid = patientUuid,
        identifier = identifier,
        openedFrom = screenKey))
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

    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = currentFacility.name,
        continuation = ContinueToScreen_Old(createEditPatientScreenKey(patientSummaryProfile))
    ))
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

  override fun openContactDoctorSheet(patientUuid: UUID) {
    val intent = ContactDoctorSheet.intent(requireContext(), patientUuid)
    requireContext().startActivity(intent)
  }

  override fun showTeleconsultButton() {
    teleconsultButton.visibility = View.VISIBLE
  }

  override fun hideTeleconsultButton() {
    teleconsultButton.visibility = View.GONE
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

  override fun hideDoneButton() {
    doneButtonFrame.visibility = View.GONE
  }

  override fun showTeleconsultLogButton() {
    logTeleconsultButtonFrame.visibility = View.VISIBLE
  }

  override fun navigateToTeleconsultRecordScreen(patientUuid: UUID, teleconsultRecordId: UUID) {
    router.push(TeleconsultRecordScreenKey(patientUuid, teleconsultRecordId))
  }

  interface Injector {
    fun inject(target: PatientSummaryScreen)
  }
}

@Parcelize
private data class ScheduleAppointmentSheetExtra(
    val sheetOpenedFrom: AppointmentSheetOpenedFrom
) : Parcelable
