package org.simple.clinic.drugs

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.RxView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class PrescribedDrugsEntryScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  companion object {
    val KEY: (UUID) -> PrescribedDrugsEntryScreenKey = ::PrescribedDrugsEntryScreenKey
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PrescribedDrugsEntryController

  private val toolbar by bindView<Toolbar>(R.id.prescribeddrugs_toolbar)
  private val recyclerView by bindView<RecyclerView>(R.id.prescribeddrugs_recyclerview)

  private val groupieAdapter = GroupAdapter<ViewHolder>()
  private val adapterUiEvents = PublishSubject.create<UiEvent>()

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    toolbar.setNavigationOnClickListener { screenRouter.pop() }
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = groupieAdapter

    Observable.mergeArray(screenCreates(), adapterUiEvents)
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PrescribedDrugsEntryScreenKey>(this)!!
    return Observable.just(PrescribedDrugsEntryScreenCreated(screenKey.patientUuid))
  }

  fun populateDrugsList(protocolDrugItems: List<ProtocolDrugSelectionItem>) {
    Timber.i("Sending drugs: $protocolDrugItems")

    val adapterItems = ArrayList<GroupieItemWithUiEvents<out ViewHolder>>()
    adapterItems += protocolDrugItems

    // Not the best way for registering click listeners,
    // but Groupie doesn't seem to have a better option.
    adapterItems.forEach { it.uiEvents = adapterUiEvents }

    groupieAdapter.update(protocolDrugItems)
  }
}
