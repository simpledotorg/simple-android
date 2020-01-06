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
import io.reactivex.rxkotlin.ofType
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.screen_patient_summary.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.PrescribedDrugsScreenKey
import org.simple.clinic.editpatient.EditPatientScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.main.TheActivity
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
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
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class PatientSummaryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), PatientSummaryScreenUi {

  companion object {
    const val REQCODE_BP_ENTRY = 1
    const val REQCODE_SCHEDULE_APPOINTMENT = 2
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controllerFactory: PatientSummaryScreenController.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  @field:[Inject Named("exact_date")]
  lateinit var exactDateFormatter: DateTimeFormatter

  @Inject
  lateinit var timestampGenerator: RelativeTimestampGenerator

  @Inject
  lateinit var config: PatientSummaryConfig

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var zoneId: ZoneId

  @field:[Inject Named("time_for_bps_recorded")]
  lateinit var timeFormatterForBp: DateTimeFormatter

  @Deprecated("""
    ~ DOA ~

    This is a quick work around that passes patient information to the Edit Patient screen so
    that the Edit Patient screen doesn't have to re-query this information from the database.

    This needs to go when we refactor this screen to Mobius and an appropriate mechanism to pass
    this information should be built.
    """)
  private var patientSummaryProfile: PatientSummaryProfile? = null

  private var linkIdWithPatientShown: Boolean = false

  override fun onSaveInstanceState(): Parcelable {
    return PatientSummaryScreenSavedState(
        super.onSaveInstanceState(),
        linkIdWithPatientShown = linkIdWithPatientShown)
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    val savedState = state as PatientSummaryScreenSavedState
    linkIdWithPatientShown = savedState.linkIdWithPatientShown

    super.onRestoreInstanceState(savedState.superSavedState)
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

    setupEditButtonClicks()

    val controller = with(screenRouter.key<PatientSummaryScreenKey>(this)) {
      controllerFactory.create(patientUuid)
    }

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            backClicks(),
            doneClicks(),
            bloodPressureSaves(),
            appointmentScheduleSheetClosed(),
            identifierLinkedEvents(),
            identifierLinkCancelledEvents(),
            updateDrugsClicks(),
            cvHistoryAnswerToggles(),
            editMeasurementClicks(),
            newBpClicks()
        ),
        controller = controller,
        screenDestroys = this.detaches().map { ScreenDestroyed() }
    )
  }

  private fun setupEditButtonClicks() {
    editButton.setOnClickListener {
      screenRouter.push(createEditPatientScreenKey(patientSummaryProfile!!))
    }
  }

  private fun createEditPatientScreenKey(
      patientSummaryProfile: PatientSummaryProfile
  ): EditPatientScreenKey {
    return EditPatientScreenKey.fromPatientData(
        patientSummaryProfile.patient,
        patientSummaryProfile.address,
        patientSummaryProfile.phoneNumber.toNullable()
    )
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PatientSummaryScreenKey>(this)
    return Observable.just(PatientSummaryScreenCreated(screenKey.patientUuid, screenKey.intention, screenKey.screenCreatedTimestamp))
  }

  private fun doneClicks() = doneButtonFrame.button.clicks().map { PatientSummaryDoneClicked() }

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
        .map { PatientSummaryBackClicked() }
  }

  private fun bloodPressureSaves() = screenRouter.streamScreenResults()
      .ofType<ActivityResult>()
      .filter { it.requestCode == REQCODE_BP_ENTRY && it.succeeded() }
      .filter { BloodPressureEntrySheet.wasBloodPressureSaved(it.data!!) }
      .map { PatientSummaryBloodPressureSaved }

  private fun appointmentScheduleSheetClosed() = screenRouter.streamScreenResults()
      .ofType<ActivityResult>()
      .filter { it.requestCode == REQCODE_SCHEDULE_APPOINTMENT && it.succeeded() }
      .map { ScheduleAppointmentSheetClosed() }

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

  private fun updateDrugsClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      drugSummaryView.updateClicked = { emitter.onNext(PatientSummaryUpdateDrugsClicked()) }

      emitter.setCancellable { drugSummaryView.updateClicked = null }
    }
  }

  private fun cvHistoryAnswerToggles(): Observable<UiEvent> {
    return Observable.create { emitter ->
      medicalHistorySummaryView.answerToggled = { question, newAnswer -> emitter.onNext(SummaryMedicalHistoryAnswerToggled(question, newAnswer)) }

      emitter.setCancellable { medicalHistorySummaryView.answerToggled = null }
    }
  }

  private fun editMeasurementClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      bloodPressureSummaryView.editMeasurementClicked = { measurement -> emitter.onNext(PatientSummaryBpClicked(measurement)) }

      emitter.setCancellable { bloodPressureSummaryView.editMeasurementClicked = null }
    }
  }

  private fun newBpClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      bloodPressureSummaryView.newBpClicked = { emitter.onNext(PatientSummaryNewBpClicked()) }

      emitter.setCancellable { bloodPressureSummaryView.newBpClicked = null }
    }
  }

  @SuppressLint("SetTextI18n")
  override fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile) {
    val patient = patientSummaryProfile.patient
    val ageValue = DateOfBirth.fromPatient(patient, userClock).estimateAge(userClock)

    displayNameGenderAge(patient.fullName, patient.gender, ageValue)
    displayPhoneNumber(patientSummaryProfile.phoneNumber.toNullable())
    displayPatientAddress(patientSummaryProfile.address)
    displayBpPassport(patientSummaryProfile.bpPassport, patientSummaryProfile.phoneNumber.isNotEmpty())

    this.patientSummaryProfile = patientSummaryProfile
  }

  private fun displayPatientAddress(address: PatientAddress) {
    addressTextView.text = when {
      address.colonyOrVillage.isNullOrBlank() -> resources.getString(R.string.patientsummary_address_without_colony, address.district, address.state)
      else -> resources.getString(R.string.patientsummary_address_with_colony, address.colonyOrVillage, address.district, address.state)
    }
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
    fullNameTextView.text = resources.getString(R.string.patientsummary_name, name, genderLetter, age)
  }

  private fun displayBpPassport(bpPassport: Optional<BusinessId>, isPhoneNumberVisible: Boolean) {
    bpPassportTextView.visibleOrGone(bpPassport.isNotEmpty())

    bpPassportTextView.text = when (bpPassport) {
      None -> ""
      is Just -> {
        val identifier = bpPassport.value.identifier
        val numericSpan = TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Numeric_White72)
        val formattedIdentifier = Truss()
            .append(identifierDisplayAdapter.typeAsText(identifier))
            .append(": ")
            .pushSpan(numericSpan)
            .append(identifierDisplayAdapter.valueAsText(identifier))
            .popSpan()
            .build()

        if (isPhoneNumberVisible) "${Unicode.bullet}  $formattedIdentifier" else formattedIdentifier
      }
    }
  }

  override fun populateList(
      prescribedDrugs: List<PrescribedDrug>,
      bloodPressureMeasurements: List<BloodPressureMeasurement>,
      medicalHistory: MedicalHistory
  ) {
    drugSummaryView.bind(
        prescriptions = prescribedDrugs,
        dateFormatter = exactDateFormatter,
        userClock = userClock
    )

    medicalHistorySummaryView.bind(
        medicalHistory = medicalHistory,
        lastUpdatedAt = timestampGenerator.generate(medicalHistory.updatedAt, userClock),
        dateFormatter = exactDateFormatter
    )

    bloodPressureSummaryView.render(
        bloodPressureMeasurements = bloodPressureMeasurements,
        utcClock = utcClock,
        placeholderLimit = config.numberOfBpPlaceholders,
        timestampGenerator = timestampGenerator,
        dateFormatter = exactDateFormatter,
        canEditFor = config.bpEditableDuration,
        bpTimeFormatter = timeFormatterForBp,
        zoneId = zoneId,
        userClock = userClock
    )
  }

  override fun showBloodPressureEntrySheet(patientUuid: UUID) {
    val intent = BloodPressureEntrySheet.intentForNewBp(context, patientUuid)
    activity.startActivityForResult(intent, REQCODE_BP_ENTRY)
  }

  override fun showBloodPressureUpdateSheet(bloodPressureMeasurementUuid: UUID) {
    val intent = BloodPressureEntrySheet.intentForUpdateBp(context, bloodPressureMeasurementUuid)
    activity.startActivity(intent)
  }

  override fun showScheduleAppointmentSheet(patientUuid: UUID) {
    val intent = ScheduleAppointmentSheet.intent(context, patientUuid)
    activity.startActivityForResult(intent, REQCODE_SCHEDULE_APPOINTMENT)
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

  override fun showUpdatePrescribedDrugsScreen(patientUuid: UUID) {
    screenRouter.push(PrescribedDrugsScreenKey(patientUuid))
  }

  override fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier) {
    if (!linkIdWithPatientShown) {
      linkIdWithPatientShown = true
      linkIdWithPatientView.downstreamUiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))
      linkIdWithPatientView.show { linkIdWithPatientView.visibility = View.VISIBLE }
    }
  }

  override fun hideLinkIdWithPatientView() {
    linkIdWithPatientView.hide { linkIdWithPatientView.visibility = View.GONE }
  }

  override fun showEditButton() {
    editButton.visibility = View.VISIBLE
  }
}

@Parcelize
data class PatientSummaryScreenSavedState(
    val superSavedState: Parcelable?,
    val linkIdWithPatientShown: Boolean
) : Parcelable

data class PatientSummaryProfile(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: Optional<PatientPhoneNumber>,
    val bpPassport: Optional<BusinessId>
)
