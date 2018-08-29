package org.simple.clinic.home.overdue

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RelativeLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import javax.inject.Inject

class OverdueScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = OverdueScreenKey()
  }

  @Inject
  lateinit var controller: OverdueScreenController

  private val overdueRecyclerView by bindView<RecyclerView>(R.id.overdue_list)
  private val emptyOverdueListView by bindView<LinearLayout>(R.id.overdue_list_empty_layout)

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    setupOverdueList()

    screenCreates()
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun setupOverdueList() {
    overdueRecyclerView.adapter = OverdueListAdapter()
    overdueRecyclerView.layoutManager = LinearLayoutManager(context)
  }

  private fun screenCreates() = Observable.just(OverdueScreenCreated())

  fun updateOverdueList() {

  }
}
