package org.simple.clinic.search.results

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class PatientSearchResultsScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSearchResultsController

  @Inject
  lateinit var utcClock: UtcClock

  private val adapter = GroupAdapter<ViewHolder>()
  private val adapterUiEvents: Subject<UiEvent> = PublishSubject.create()

  private val toolbar by bindView<Toolbar>(R.id.patientsearchresults_toolbar)
  private val recyclerView by bindView<RecyclerView>(R.id.patientsearchresults_results)
  private val emptyStateView by bindView<View>(R.id.patientsearchresults_empty_state)
  private val newPatientRationaleTextView by bindView<TextView>(R.id.patientsearchresults_new_patient_rationale)
  private val newPatientButton by bindView<Button>(R.id.patientsearchresults_new_patient)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)
    setupScreen()

    Observable
        .mergeArray(screenCreates(), newPatientClicks(), adapterUiEvents)
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { it(this) }
  }

  private fun setupScreen() {
    hideKeyboard()
    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }
    toolbar.setOnClickListener {
      screenRouter.pop()
    }

    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = adapter

    val screenKey = screenRouter.key<PatientSearchResultsScreenKey>(this)
    toolbar.title = screenKey.fullName
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PatientSearchResultsScreenKey>(this)
    return Observable.just(PatientSearchResultsScreenCreated(screenKey))
  }

  private fun newPatientClicks() =
      RxView
          .clicks(newPatientButton)
          .map { CreateNewPatientClicked() }

  fun updateSearchResults(results: List<PatientSearchResultsItemType<out ViewHolder>>) {
    results.forEach { it.uiEvents = adapterUiEvents }
    adapter.update(results)
  }

  fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewExistingPatient, Instant.now(utcClock)))
  }

  fun openPatientEntryScreen() {
    screenRouter.push(PatientEntryScreenKey())
  }

  fun setEmptyStateVisible(visible: Boolean) {
    emptyStateView.visibleOrGone(visible)

    newPatientRationaleTextView.setText(when {
      visible -> R.string.patientsearchresults_register_patient_rationale_for_empty_state
      else -> R.string.patientsearchresults_register_patient_rationale
    })
    newPatientButton.setText(when {
      visible -> R.string.patientsearchresults_register_patient_for_empty_state
      else -> R.string.patientsearchresults_register_patient
    })
  }

}
