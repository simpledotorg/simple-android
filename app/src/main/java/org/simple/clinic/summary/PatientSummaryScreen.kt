package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
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
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.TheActivity
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.drugs.selection.PrescribedDrugsScreen
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.RouterDirection.BACKWARD
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.executeOnNextMeasure
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.setBottomPadding
import java.util.UUID
import javax.inject.Inject

class PatientSummaryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY: (patientUuid: UUID, PatientSummaryCaller) -> PatientSummaryScreenKey = ::PatientSummaryScreenKey
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
  private val doneButton by bindView<ViewGroup>(R.id.patientsummary_done)

  private val recyclerViewAdapter = GroupAdapter<ViewHolder>()
  private val prescriptionSection: Section = Section()
  private val bloodPressureSection: Section = Section()
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

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    // Not sure why but the keyboard stays visible when coming from search.
    rootLayout.hideKeyboard()

    setupSummaryList()

    Observable.mergeArray(screenCreates(), backClicks(), doneClicks(), adapterUiEvents)
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PatientSummaryScreenKey>(this)!!
    return Observable.just(PatientSummaryScreenCreated(screenKey.patientUuid, screenKey.caller))
  }

  private fun doneClicks() = RxView.clicks(doneButton).map { PatientSummaryDoneClicked() }

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

  @SuppressLint("SetTextI18n")
  fun populatePatientProfile(patient: Patient, address: PatientAddress, phoneNumber: Optional<PatientPhoneNumber>) {
    fullNameTextView.text = patient.fullName
    byline1TextView.text = when (phoneNumber) {
      is Just -> "${resources.getString(Gender.MALE.displayTextRes)} • ${phoneNumber.value.number}"
      is None -> resources.getString(Gender.MALE.displayTextRes)
    }
    byline2TextView.text = when {
      address.colonyOrVillage.isNullOrBlank() -> "${address.district}, ${address.state}"
      else -> "${address.colonyOrVillage}, ${address.district}, ${address.state}"
    }
  }

  private fun setupSummaryList() {
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = recyclerViewAdapter

    val emptySummaryItem = SummaryPrescribedDrugsItem(listOf())
    recyclerViewAdapter.add(prescriptionSection)
    populatePrescribedDrugsSummary(emptySummaryItem)

    val newBpItem = SummaryAddNewBpItem()
    newBpItem.uiEvents = adapterUiEvents
    bloodPressureSection.setHeader(newBpItem)
    recyclerViewAdapter.add(bloodPressureSection)
  }

  fun populatePrescribedDrugsSummary(prescribedDrugsItem: SummaryPrescribedDrugsItem) {
    // Not the best way for registering click listeners,
    // but Groupie doesn't seem to have a better option.
    prescribedDrugsItem.uiEvents = adapterUiEvents
    prescriptionSection.update(listOf(prescribedDrugsItem))
  }

  fun populateBloodPressureHistory(measurementItems: List<SummaryBloodPressureItem>) {
    // Skip item animations on the first update.
    if (recyclerViewAdapter.itemCount != 0) {
      val animator = SlideUpAlphaAnimator().withInterpolator(FastOutSlowInInterpolator())
      animator.supportsChangeAnimations = false
      recyclerView.itemAnimator = animator
    }

    measurementItems.forEach { it.uiEvents = adapterUiEvents }
    bloodPressureSection.update(measurementItems)
  }

  fun showBloodPressureEntrySheetIfNotShownAlready(patientUuid: UUID) {
    // FIXME: This is a really ugly workaround. Shouldn't be managing state like this.
    if (!bpEntrySheetAlreadyShownOnStart) {
      bpEntrySheetAlreadyShownOnStart = true
      activity.startActivity(BloodPressureEntrySheet.intent(context, patientUuid))
    }
  }

  fun showBloodPressureEntrySheet(patientUuid: UUID) {
    activity.startActivity(BloodPressureEntrySheet.intent(context, patientUuid))
  }

  fun showUpdatePrescribedDrugsScreen(patientUuid: UUID) {
    screenRouter.push(PrescribedDrugsScreen.KEY(patientUuid))
  }

  fun setDoneButtonVisible(visible: Boolean) {
    if (visible) {
      doneButton.executeOnNextMeasure {
        recyclerView.setBottomPadding(doneButton.height)
      }
    }
    doneButton.visibility = when {
      visible -> View.VISIBLE
      else -> View.GONE
    }
  }

  fun goBackToPatientSearch() {
    screenRouter.pop()
  }

  fun goBackToHome() {
    screenRouter.clearHistoryAndPush(HomeScreen.KEY, direction = BACKWARD)
  }
}
