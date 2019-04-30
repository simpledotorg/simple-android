package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.drugs.selection.PrescribedDrugsScreenKey
import org.simple.clinic.editpatient.PatientEditScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.RouterDirection.BACKWARD
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.linkId.LinkIdWithPatientCancelled
import org.simple.clinic.summary.linkId.LinkIdWithPatientLinked
import org.simple.clinic.summary.linkId.LinkIdWithPatientView
import org.simple.clinic.summary.linkId.LinkIdWithPatientViewShown
import org.simple.clinic.summary.updatephone.UpdatePhoneNumberDialog
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID
import javax.inject.Inject

class PatientSummaryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    const val REQCODE_BP_ENTRY = 1
    const val REQCODE_SCHEDULE_APPOINTMENT = 2
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSummaryScreenController

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var clock: UtcClock

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  private val rootLayout by bindView<ViewGroup>(R.id.patientsummary_root)
  private val backButton by bindView<ImageButton>(R.id.patientsummary_back)
  private val fullNameTextView by bindView<TextView>(R.id.patientsummary_fullname)
  private val addressTextView by bindView<TextView>(R.id.patientsummary_address)
  private val contactTextView by bindView<TextView>(R.id.patientsummary_contact)
  private val recyclerView by bindView<RecyclerView>(R.id.patientsummary_recyclerview)
  private val doneButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.patientsummary_done)
  private val editButton by bindView<Button>(R.id.patientsummary_edit)
  private val bpPassportTextView by bindView<TextView>(R.id.patientsummary_bp_passport)
  private val linkIdWithPatientView by bindView<LinkIdWithPatientView>(R.id.patientsummary_linkidwithpatient)
  private val recyclerViewAdapter = GroupAdapter<ViewHolder>()
  private val prescriptionSection = Section()
  private val bloodPressureSection = Section()
  private val medicalHistorySection = Section()
  private val adapterUiEvents = PublishSubject.create<UiEvent>()

  private var bpEntrySheetAlreadyShownOnStart: Boolean = false
  private var linkIdWithPatientShown: Boolean = false

  override fun onSaveInstanceState(): Parcelable {
    return PatientSummaryScreenSavedState(
        super.onSaveInstanceState(),
        bpEntryShownOnStart = bpEntrySheetAlreadyShownOnStart,
        linkIdWithPatientShown = linkIdWithPatientShown)
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    val (superSavedState, bpEntryShownOnStart, waslinkIdWithPatientShown) = state as PatientSummaryScreenSavedState
    bpEntrySheetAlreadyShownOnStart = bpEntryShownOnStart
    linkIdWithPatientShown = waslinkIdWithPatientShown

    super.onRestoreInstanceState(superSavedState)
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

    setupSummaryList()
    setupEditButtonClicks()

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            backClicks(),
            doneClicks(),
            adapterUiEvents,
            bloodPressureSaves(),
            appointmentScheduledSuccess(),
            appointmentScheduleSheetClosed(),
            identifierLinkedEvents(),
            identifierLinkCancelledEvents()
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun setupEditButtonClicks() {
    editButton.setOnClickListener {
      val key = screenRouter.key<PatientSummaryScreenKey>(this)

      screenRouter.push(PatientEditScreenKey(key.patientUuid))
    }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PatientSummaryScreenKey>(this)
    return Observable.just(PatientSummaryScreenCreated(screenKey.patientUuid, screenKey.intention, screenKey.screenCreatedTimestamp))
  }

  private fun doneClicks() = RxView.clicks(doneButtonFrame.button).map { PatientSummaryDoneClicked() }

  private fun backClicks(): Observable<UiEvent> {
    val hardwareBackKeyClicks = Observable.create<Any> { emitter ->
      val interceptor = object : BackPressInterceptor {
        override fun onInterceptBackPress(callback: BackPressInterceptCallback) {
          emitter.onNext(Any())
          callback.markBackPressIntercepted()
        }
      }
      emitter.setCancellable { screenRouter.unregisterBackPressInterceptor(interceptor) }
      screenRouter.registerBackPressInterceptor(interceptor)
    }

    return RxView.clicks(backButton)
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

  private fun appointmentScheduledSuccess() = screenRouter.streamScreenResults()
      .ofType<ActivityResult>()
      .filter { it.requestCode == REQCODE_SCHEDULE_APPOINTMENT && it.succeeded() && it.data != null }
      .map { ScheduleAppointmentSheet.appointmentSavedDate(it.data!!) }
      .map { AppointmentScheduled }

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

  @SuppressLint("SetTextI18n")
  fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile) {
    val patient = patientSummaryProfile.patient
    val ageValue = when {
      patient.dateOfBirth == null -> {
        patient.age!!.let { age ->
          estimateCurrentAge(age.value, age.updatedAt, clock)
        }
      }
      else -> {
        estimateCurrentAge(patient.dateOfBirth, clock)
      }
    }

    displayNameGenderAge(patient.fullName, patient.gender, ageValue)
    displayPhoneNumber(patientSummaryProfile.phoneNumber.toNullable())
    displayPatientAddress(patientSummaryProfile.address)
    displayBpPassport(patientSummaryProfile.bpPassport, patientSummaryProfile.phoneNumber.isNotEmpty())
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

  private fun setupSummaryList() {
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = recyclerViewAdapter

    val newBpItem = SummaryAddNewBpListItem()
    newBpItem.uiEvents = adapterUiEvents
    bloodPressureSection.setHeader(newBpItem)
  }

  fun populateList(
      prescribedDrugsItem: SummaryPrescribedDrugsItem,
      measurementPlaceholderItems: List<SummaryBloodPressurePlaceholderListItem>,
      measurementItems: List<SummaryBloodPressureListItem>,
      medicalHistoryItem: SummaryMedicalHistoryItem
  ) {
    // Skip item animations on the first update.
    val isFirstUpdate = recyclerViewAdapter.itemCount == 0
    if (isFirstUpdate.not()) {
      val animator = SlideUpAlphaAnimator().withInterpolator(FastOutSlowInInterpolator())
      animator.supportsChangeAnimations = false
      recyclerView.itemAnimator = animator
    }

    // Not the best way for registering click listeners,
    // but Groupie doesn't seem to have a better option.
    prescribedDrugsItem.uiEvents = adapterUiEvents
    measurementItems.forEach { it.uiEvents = adapterUiEvents }
    medicalHistoryItem.uiEvents = adapterUiEvents

    prescriptionSection.update(listOf(prescribedDrugsItem))
    if (isFirstUpdate) {
      recyclerViewAdapter.add(prescriptionSection)
    }

    bloodPressureSection.update(measurementItems + measurementPlaceholderItems)
    if (isFirstUpdate) {
      recyclerViewAdapter.add(bloodPressureSection)
    }

    medicalHistorySection.update(listOf(medicalHistoryItem))
    if (isFirstUpdate) {
      recyclerViewAdapter.add(medicalHistorySection)
    }
  }

  fun showBloodPressureEntrySheetIfNotShownAlready(patientUuid: UUID) {
    // FIXME: This is a really ugly workaround. Shouldn't be managing state like this.
    if (!bpEntrySheetAlreadyShownOnStart) {
      bpEntrySheetAlreadyShownOnStart = true
      showBloodPressureEntrySheet(patientUuid)
    }
  }

  fun showBloodPressureEntrySheet(patientUuid: UUID) {
    val intent = BloodPressureEntrySheet.intentForNewBp(context, patientUuid)
    activity.startActivityForResult(intent, REQCODE_BP_ENTRY)
  }

  fun showBloodPressureUpdateSheet(bloodPressureMeasurementUuid: UUID) {
    val intent = BloodPressureEntrySheet.intentForUpdateBp(context, bloodPressureMeasurementUuid)
    activity.startActivity(intent)
  }

  fun showScheduleAppointmentSheet(patientUuid: UUID) {
    val intent = ScheduleAppointmentSheet.intent(context, patientUuid)
    activity.startActivityForResult(intent, REQCODE_SCHEDULE_APPOINTMENT)
  }

  fun goToPreviousScreen() {
    screenRouter.pop()
  }

  fun goToHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey(), direction = BACKWARD)
  }

  fun showUpdatePhoneDialog(patientUuid: UUID) {
    UpdatePhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
  }

  fun showAddPhoneDialog(patientUuid: UUID) {
    AddPhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
  }

  fun showUpdatePrescribedDrugsScreen(patientUuid: UUID) {
    screenRouter.push(PrescribedDrugsScreenKey(patientUuid))
  }

  fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier) {
    if (!linkIdWithPatientShown) {
      linkIdWithPatientShown = true
      linkIdWithPatientView.downstreamUiEvents.onNext(LinkIdWithPatientViewShown(patientUuid, identifier))
      linkIdWithPatientView.show { linkIdWithPatientView.visibility = View.VISIBLE }
    }
  }

  fun hideLinkIdWithPatientView() {
    linkIdWithPatientView.hide { linkIdWithPatientView.visibility = View.GONE }
  }
}

@Parcelize
data class PatientSummaryScreenSavedState(
    val superSavedState: Parcelable?,
    val bpEntryShownOnStart: Boolean,
    val linkIdWithPatientShown: Boolean
) : Parcelable

data class PatientSummaryProfile(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: Optional<PatientPhoneNumber>,
    val bpPassport: Optional<BusinessId>
)
