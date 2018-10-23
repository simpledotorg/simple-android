package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
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
import org.simple.clinic.drugs.selection.PrescribedDrugsScreen
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.RouterDirection.BACKWARD
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import java.util.UUID
import javax.inject.Inject

class PatientSummaryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = ::PatientSummaryScreenKey

    const val REQCODE_BP_SAVED = 1
    const val REQCODE_SCHEDULE_APPOINTMENT = 2
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSummaryScreenController

  @Inject
  lateinit var activity: TheActivity

  private val rootLayout by bindView<ViewGroup>(R.id.patientsummary_root)
  private val backButton by bindView<ImageButton>(R.id.patientsummary_back)
  private val fullNameTextView by bindView<TextView>(R.id.patientsummary_fullname)
  private val byline1TextView by bindView<TextView>(R.id.patientsummary_byline1)
  private val byline2TextView by bindView<TextView>(R.id.patientsummary_byline2)
  private val recyclerView by bindView<RecyclerView>(R.id.patientsummary_recyclerview)
  private val doneButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.patientsummary_done)

  private val recyclerViewAdapter = GroupAdapter<ViewHolder>()
  private val prescriptionSection = Section()
  private val bloodPressureSection = Section()
  private val medicalHistorySection = Section()
  private val adapterUiEvents = PublishSubject.create<UiEvent>()
  private val bpWasSavedEvents = PublishSubject.create<UiEvent>()

  private var bpEntrySheetAlreadyShownOnStart: Boolean = false
  private var bloodPressureWasSaved: Boolean = false

  override fun onSaveInstanceState(): Parcelable {
    return PatientSummaryScreenSavedState(
        super.onSaveInstanceState(),
        bpEntryShownOnStart = bpEntrySheetAlreadyShownOnStart,
        bloodPressureWasSaved = bloodPressureWasSaved)
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    val (superSavedState, bpEntryShownOnStart, bpWasSaved) = state as PatientSummaryScreenSavedState
    bpEntrySheetAlreadyShownOnStart = bpEntryShownOnStart
    bloodPressureWasSaved = bpWasSaved

    bpWasSavedEvents.onNext(PatientSummaryRestoredWithBPSaved(bpWasSaved))

    super.onRestoreInstanceState(superSavedState)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    // Not sure why but the keyboard stays visible when coming from search.
    rootLayout.hideKeyboard()

    setupSummaryList()

    controller.disposeOnDetach(this)

    Observable
        .mergeArray(
            screenCreates(),
            backClicks(),
            doneClicks(),
            adapterUiEvents,
            bloodPressureSaves(),
            bpWasSavedEvents,
            appointmentScheduleSheetClosed())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PatientSummaryScreenKey>(this)
    return Observable.just(PatientSummaryScreenCreated(screenKey.patientUuid, screenKey.caller))
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
      .filter { it.requestCode == REQCODE_BP_SAVED && it.succeeded() }
      .map {BloodPressureEntrySheet.wasBloodPressureSaved(it.data!!)}
      .doOnNext { saved -> bloodPressureWasSaved = saved }
      .map { saved -> PatientSummaryBloodPressureClosed(saved) }

  private fun appointmentScheduleSheetClosed() = screenRouter.streamScreenResults()
      .ofType<ActivityResult>()
      .filter { it.requestCode == REQCODE_SCHEDULE_APPOINTMENT && it.succeeded() }
      .map { ScheduleAppointmentSheetClosed() }

  @SuppressLint("SetTextI18n")
  fun populatePatientProfile(patient: Patient, address: PatientAddress, phoneNumber: Optional<PatientPhoneNumber>) {
    fullNameTextView.text = patient.fullName

    if (patient.dateOfBirth == null) {
      val computedDob = patient.age!!.computedDateOfBirth
      val years = Period.between(computedDob, LocalDate.now()).years.toString()
      byline1TextView.text = years

    } else {
      val years = Period.between(patient.dateOfBirth, LocalDate.now()).years.toString()
      byline1TextView.text = years
    }

    byline1TextView.text = when (patient.gender) {
      Gender.MALE -> "${byline1TextView.text}, ${resources.getString(Gender.MALE.displayTextRes)}"
      Gender.FEMALE -> "${byline1TextView.text}, ${resources.getString(Gender.FEMALE.displayTextRes)}"
      Gender.TRANSGENDER -> "${byline1TextView.text}, ${resources.getString(Gender.TRANSGENDER.displayTextRes)}"
    }

    byline1TextView.text = when (phoneNumber) {
      is Just -> "${byline1TextView.text} • ${phoneNumber.value.number}"
      is None -> byline1TextView.text
    }

    byline2TextView.text = when {
      address.colonyOrVillage.isNullOrBlank() -> "${address.district}, ${address.state}"
      else -> "${address.colonyOrVillage}, ${address.district}, ${address.state}"
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
    activity.startActivityForResult(BloodPressureEntrySheet.intent(context, patientUuid), REQCODE_BP_SAVED)
  }

  fun showBloodPressureUpdateSheet(bloodPressureMeasurementUuid: UUID) {

  }

  fun showUpdatePrescribedDrugsScreen(patientUuid: UUID) {
    screenRouter.push(PrescribedDrugsScreen.KEY(patientUuid))
  }

  fun showScheduleAppointmentSheet(patientUuid: UUID) {
    activity.startActivityForResult(ScheduleAppointmentSheet.intent(context, patientUuid), REQCODE_SCHEDULE_APPOINTMENT)
  }

  fun goBackToPatientSearch() {
    screenRouter.pop()
  }

  fun goBackToHome() {
    screenRouter.clearHistoryAndPush(HomeScreen.KEY, direction = BACKWARD)
  }
}

@Parcelize
data class PatientSummaryScreenSavedState(
    val superSavedState: Parcelable,
    val bpEntryShownOnStart: Boolean,
    val bloodPressureWasSaved: Boolean
) : Parcelable
