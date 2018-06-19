package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.content.Context
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
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.TheActivity
import org.simple.clinic.bp.entry.BloodPressureEntrySheetView
import org.simple.clinic.drugs.selection.PrescribedDrugsEntryScreen
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.router.screen.RouterDirection.BACKWARD
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
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

  private val groupieAdapter = GroupAdapter<ViewHolder>()
  private val adapterUiEvents = PublishSubject.create<UiEvent>()

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    // Not sure why but the keyboard stays visible when coming from search.
    rootLayout.hideKeyboard()

    Observable.mergeArray(screenCreates(), backClicks(), adapterUiEvents)
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

  private fun backClicks() = RxView.clicks(backButton).map { PatientSummaryBackClicked() }

  @SuppressLint("SetTextI18n")
  fun populatePatientInfo(patient: Patient, address: PatientAddress, phoneNumber: Optional<PatientPhoneNumber>) {
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

  fun setupSummaryList() {
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = groupieAdapter
  }

  fun populateSummaryList(measurementItems: List<SummaryBloodPressureItem>) {
    // Skip item animations on the first update.
    if (groupieAdapter.itemCount != 0) {
      val animator = SlideUpAlphaAnimator().withInterpolator(FastOutSlowInInterpolator())
      animator.supportsChangeAnimations = false
      recyclerView.itemAnimator = animator
    }

    val adapterItems = ArrayList<GroupieItemWithUiEvents<out ViewHolder>>()
    adapterItems += SummaryPrescribedDrugsItem()
    adapterItems += SummaryAddNewBpItem()
    adapterItems += measurementItems

    // Not the best way for registering click listeners,
    // but Groupie doesn't seem to have a better option.
    adapterItems.forEach { it.uiEvents = adapterUiEvents }
    groupieAdapter.update(adapterItems)
  }

  fun showBloodPressureEntrySheet(patientUuid: UUID) {
    BloodPressureEntrySheetView.showForPatient(patientUuid, activity.supportFragmentManager)
  }

  fun showUpdatePrescribedDrugsScreen(patientUuid: UUID) {
    screenRouter.push(PrescribedDrugsEntryScreen.KEY(patientUuid))
  }

  fun goBackToPatientSearch() {
    screenRouter.pop()
  }

  fun goBackToHome() {
    screenRouter.clearHistoryAndPush(HomeScreen.KEY, direction = BACKWARD)
  }
}
