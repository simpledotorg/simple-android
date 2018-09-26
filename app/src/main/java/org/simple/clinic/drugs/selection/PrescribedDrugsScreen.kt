package org.simple.clinic.drugs.selection

import android.content.Context
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.RxView
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.entry.CustomPrescriptionEntrySheet
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class PrescribedDrugsScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  companion object {
    val KEY: (UUID) -> PrescribedDrugsScreenKey = ::PrescribedDrugsScreenKey
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PrescribedDrugsEntryController

  @Inject
  lateinit var activity: TheActivity

  private val toolbar by bindView<Toolbar>(R.id.prescribeddrugs_toolbar)
  private val recyclerView by bindView<RecyclerView>(R.id.prescribeddrugs_recyclerview)
  private val doneButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.prescribeddrugs_done)

  private val groupieAdapter = GroupAdapter<ViewHolder>()
  private val adapterUiEvents = PublishSubject.create<UiEvent>()

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    toolbar.setNavigationOnClickListener { screenRouter.pop() }
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = groupieAdapter

    val fadeAnimator = DefaultItemAnimator()
    fadeAnimator.supportsChangeAnimations = false
    recyclerView.itemAnimator = fadeAnimator

    Observable.mergeArray(screenCreates(), adapterUiEvents, doneClicks())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PrescribedDrugsScreenKey>(this)
    return Observable.just(PrescribedDrugsScreenCreated(screenKey.patientUuid))
  }

  private fun doneClicks() = RxView.clicks(doneButtonFrame.button).map { PrescribedDrugsDoneClicked() }

  fun populateDrugsList(protocolDrugItems: List<GroupieItemWithUiEvents<out ViewHolder>>) {
    // Replace the default fade animator with another animator that
    // plays change animations together instead of sequentially.
    if (groupieAdapter.itemCount != 0) {
      val animator = SlideUpAlphaAnimator().withInterpolator(FastOutSlowInInterpolator())
      animator.supportsChangeAnimations = false
      recyclerView.itemAnimator = animator
    }

    val newAdapterItems = ArrayList<GroupieItemWithUiEvents<out ViewHolder>>()
    newAdapterItems += protocolDrugItems
    newAdapterItems += AddNewPrescriptionListItem()

    // Not the best way for registering click listeners,
    // but Groupie doesn't seem to have a better option.
    newAdapterItems.forEach { it.uiEvents = adapterUiEvents }

    val hasNewItems = groupieAdapter.itemCount < newAdapterItems.size
    groupieAdapter.update(newAdapterItems)

    // Scroll to end to show newly added prescriptions.
    if (hasNewItems) {
      recyclerView.postDelayed(
          { recyclerView.smoothScrollToPosition(recyclerView.adapter.itemCount - 1) },
          300)
    }
  }

  fun showNewPrescriptionEntrySheet(patientUuid: UUID) {
    activity.startActivity(CustomPrescriptionEntrySheet.intent(context, patientUuid))
  }

  fun showDeleteConfirmationDialog(prescription: PrescribedDrug) {
    ConfirmDeletePrescriptionDialog.showForPrescription(prescription.uuid, activity.supportFragmentManager)
  }

  fun goBackToPatientSummary() {
    screenRouter.pop()
  }
}
