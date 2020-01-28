package org.simple.clinic.summary.bloodpressures.newbpsummary.view

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.patientsummary_newbpsummary_content.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.di.injector
import org.simple.clinic.summary.bloodpressures.newbpsummary.AddNewBloodPressureClicked
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewEvent
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewUi
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewUiActions
import org.simple.clinic.summary.bloodpressures.newbpsummary.SeeAllClicked
import org.simple.clinic.util.unsafeLazy
import java.util.UUID

class NewBloodPressureSummaryView(
    context: Context,
    attrs: AttributeSet
) : CardView(context, attrs), NewBloodPressureSummaryViewUi, NewBloodPressureSummaryViewUiActions {

  private val viewEvents = PublishSubject.create<NewBloodPressureSummaryViewEvent>()

  private val events: Observable<NewBloodPressureSummaryViewEvent> by unsafeLazy {
    Observable
        .merge(
            addNewBpClicked(),
            seeAllClicked(),
            viewEvents
        )
        .compose(ReportAnalyticsEvents())
        .cast<NewBloodPressureSummaryViewEvent>()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<NewBloodPressureSummaryViewInjector>().inject(this)
  }

  override fun showNoBloodPressuresView() {
  }

  override fun showBloodPressures(bloodPressures: List<BloodPressureMeasurement>) {
  }

  override fun showSeeAllButton() {
  }

  override fun hideSeeAllButton() {
  }

  override fun openBloodPressureEntrySheet(patientUuid: UUID) {
  }

  override fun openBloodPressureUpdateSheet(bpUuid: UUID) {
  }

  override fun showBloodPressureHistoryScreen(patientUuid: UUID) {
  }

  private fun addNewBpClicked(): Observable<NewBloodPressureSummaryViewEvent> {
    return addNewBP.clicks().map { AddNewBloodPressureClicked }
  }

  private fun seeAllClicked(): Observable<NewBloodPressureSummaryViewEvent> {
    return seeAll.clicks().map { SeeAllClicked }
  }
}
