package org.simple.clinic.patient.recent

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.xwray.groupie.GroupAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.ScreenCreated
import javax.inject.Inject

class RecentPatientsView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

  @Inject
  lateinit var controller: RecentPatientsViewController

  private val groupAdapter = GroupAdapter<com.xwray.groupie.ViewHolder>()

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    layoutManager = LinearLayoutManager(context)
    adapter = groupAdapter

    Observable.mergeArray(screenCreates())
        .compose(controller)
        .takeUntil(RxView.detaches(this))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  fun updateRecentPatients(recentPatients: List<RecentPatientItem>) {
    groupAdapter.update(recentPatients)
  }

  fun showNoRecentPatients(isVisible: Boolean) {
    recent_patient_no_recent_patients.visibleOrGone(isVisible)
  }
}
