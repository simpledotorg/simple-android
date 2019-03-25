package org.simple.clinic.patient.recent

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
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

  private val recyclerView by bindView<RecyclerView>(R.id.recentpatients_recyclerview)
  private val emptyStateView by bindView<View>(R.id.recentpatients_no_recent_patients)

  private val groupAdapter = GroupAdapter<ViewHolder>()
  private val adapterEvents = PublishSubject.create<UiEvent>()

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    inflate(context, R.layout.recent_patients, this)

    recyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = groupAdapter
    }

    val screenDestroys = RxView
        .detaches(this)
        .map { ScreenDestroyed() }

    Observable.mergeArray(screenCreates(), screenDestroys)
        .compose(controller)
        .takeUntil(screenDestroys)
        .observeOn(mainThread())
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

  fun showEmptyState() {
    emptyStateView.visibleOrGone(true)
  }

  fun hideEmptyState() {
    emptyStateView.visibleOrGone(false)
  }

  private fun openPatientSummary(patientUuid: UUID) {
    activity.screenRouter.push(
        PatientSummaryScreenKey(
            patientUuid = patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        ))
  }
}
