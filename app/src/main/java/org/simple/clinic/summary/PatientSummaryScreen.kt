package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
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
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.drugs.selection.PrescribedDrugsScreenKey
import org.simple.clinic.editpatient.PatientEditScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.RouterDirection.BACKWARD
import org.simple.clinic.router.screen.SCREEN_CHANGE_ANIMATION_DURATION
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.updatephone.UpdatePhoneNumberDialog
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import java.util.UUID
import java.util.concurrent.TimeUnit.MILLISECONDS
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

  private val rootLayout by bindView<ViewGroup>(R.id.patientsummary_root)
  private val backButton by bindView<ImageButton>(R.id.patientsummary_back)
  private val fullNameTextView by bindView<TextView>(R.id.patientsummary_fullname)
  private val addressTextView by bindView<TextView>(R.id.patientsummary_address)
  private val contactTextView by bindView<TextView>(R.id.patientsummary_contact)
  private val recyclerView by bindView<RecyclerView>(R.id.patientsummary_recyclerview)
  private val doneButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.patientsummary_done)
  private val editButton by bindView<Button>(R.id.patientsummary_edit)

  private val recyclerViewAdapter = GroupAdapter<ViewHolder>()
  private val prescriptionSection = Section()
  private val bloodPressureSection = Section()
  private val medicalHistorySection = Section()
  private val adapterUiEvents = PublishSubject.create<UiEvent>()

  private var bpEntrySheetAlreadyShownOnStart: Boolean = false

  override fun onSaveInstanceState(): Parcelable {
    return PatientSummaryScreenSavedState(
        super.onSaveInstanceState(),
        bpEntryShownOnStart = bpEntrySheetAlreadyShownOnStart)
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    val (superSavedState, bpEntryShownOnStart) = state as PatientSummaryScreenSavedState
    bpEntrySheetAlreadyShownOnStart = bpEntryShownOnStart

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

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }

    Observable
        .mergeArray(
            screenCreates(),
            screenDestroys,
            backClicks(),
            doneClicks(),
            adapterUiEvents,
            bloodPressureSaves(),
            appointmentScheduledSuccess(),
            appointmentScheduleSheetClosed())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(screenDestroys)
        .subscribe { uiChange -> uiChange(this) }
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

  @SuppressLint("SetTextI18n")
  fun populatePatientProfile(patient: Patient, address: PatientAddress, phoneNumber: Optional<PatientPhoneNumber>) {
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
    displayPhoneNumber(phoneNumber.toNullable())
    displayPatientAddress(address)
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

  fun goBackToPatientSearch() {
    screenRouter.pop()
  }

  fun goBackToHome() {
    screenRouter.clearHistoryAndPush(HomeScreenKey(), direction = BACKWARD)
  }

  @SuppressLint("CheckResult")
  fun showUpdatePhoneDialog(patientUuid: UUID) {
    Observable.timer(SCREEN_CHANGE_ANIMATION_DURATION, MILLISECONDS, mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe {
          UpdatePhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
        }
  }

  @SuppressLint("CheckResult")
  fun showAddPhoneDialog(patientUuid: UUID) {
    Observable.timer(SCREEN_CHANGE_ANIMATION_DURATION, MILLISECONDS, mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe {
          AddPhoneNumberDialog.show(patientUuid, activity.supportFragmentManager)
        }
  }

  fun showUpdatePrescribedDrugsScreen(patientUuid: UUID) {
    screenRouter.push(PrescribedDrugsScreenKey(patientUuid))
  }
}

@Parcelize
data class PatientSummaryScreenSavedState(
    val superSavedState: Parcelable?,
    val bpEntryShownOnStart: Boolean
) : Parcelable
