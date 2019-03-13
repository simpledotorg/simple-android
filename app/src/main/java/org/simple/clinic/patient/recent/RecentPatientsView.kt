package org.simple.clinic.patient.recent

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.xwray.groupie.GroupAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.recent_patients.view.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.summary.PatientSummaryCaller
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class RecentPatientsView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  @Inject
  lateinit var controller: RecentPatientsViewController

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var utcClock: UtcClock

  private val groupAdapter = GroupAdapter<com.xwray.groupie.ViewHolder>()

  private val adapterEvents = PublishSubject.create<UiEvent>()

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    inflate(context, R.layout.recent_patients, this)

    recent_patient_recyclerview.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = groupAdapter
    }

    Observable.mergeArray(screenCreates())
        .compose(controller)
        .takeUntil(RxView.detaches(this))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { uiChange -> uiChange(this) }

    adapterEvents
        .ofType<RecentPatientItemClicked>()
        .takeUntil(RxView.detaches(this))
        .subscribe { openPatientSummary(it.patientUuid) }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  fun updateRecentPatients(recentPatients: List<RecentPatientItem>) {
    recentPatients.forEach { it.uiEvents = adapterEvents }
    groupAdapter.update(recentPatients)
  }

  fun showNoRecentPatients() {
    recent_patient_no_recent_patients.visibleOrGone(true)
  }

  fun hideNoRecentPatients() {
    recent_patient_no_recent_patients.visibleOrGone(false)
  }

  fun clearRecentPatients() {
    groupAdapter.clear()
  }

  private fun openPatientSummary(patientUuid: UUID) {
    activity.screenRouter.push(
        PatientSummaryScreenKey(
            patientUuid = patientUuid,
            caller = PatientSummaryCaller.SEARCH,
            screenCreatedTimestamp = Instant.now(utcClock)
        ))
  }
}
