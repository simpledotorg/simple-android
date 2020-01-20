package org.simple.clinic.bp.history

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_bp_history.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItemDiffCallback
import org.simple.clinic.bp.history.adapter.Event.AddNewBpClicked
import org.simple.clinic.bp.history.adapter.Event.BloodPressureHistoryItemClicked
import org.simple.clinic.di.injector
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import java.util.UUID
import javax.inject.Inject

class BloodPressureHistoryScreen(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs), BloodPressureHistoryScreenUi, BloodPressureHistoryScreenUiActions {

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var config: PatientSummaryConfig

  private val bloodPressureHistoryAdapter = ItemAdapter(BloodPressureHistoryListItemDiffCallback())

  private val events: Observable<BloodPressureHistoryScreenEvent> by unsafeLazy {
    Observable
        .merge(
            addNewBpClicked(),
            bloodPressureClicked()
        )
        .compose(ReportAnalyticsEvents())
        .cast<BloodPressureHistoryScreenEvent>()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<BloodPressureHistoryScreenInjector>().inject(this)

    setupBloodPressureHistoryList()
  }

  private fun setupBloodPressureHistoryList() {
    bpHistoryList.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context)
      adapter = bloodPressureHistoryAdapter
    }
  }

  override fun showBloodPressureHistory(bloodPressures: List<BloodPressureMeasurement>) {
    bloodPressureHistoryAdapter.submitList(BloodPressureHistoryListItem.from(bloodPressures, config.bpEditableDuration, utcClock))
  }

  override fun openBloodPressureEntrySheet() {
  }

  override fun openBloodPressureUpdateSheet(bpUuid: UUID) {
  }

  private fun addNewBpClicked(): Observable<BloodPressureHistoryScreenEvent> {
    return bloodPressureHistoryAdapter
        .itemEvents
        .ofType<AddNewBpClicked>()
        .map { NewBloodPressureClicked }
  }

  private fun bloodPressureClicked(): Observable<BloodPressureHistoryScreenEvent> {
    return bloodPressureHistoryAdapter
        .itemEvents
        .ofType<BloodPressureHistoryItemClicked>()
        .map { it.measurement }
        .map(::BloodPressureClicked)
  }
}
