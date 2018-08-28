package org.simple.clinic.home.overdue

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import kotterknife.bindView
import org.simple.clinic.R

class OverdueScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = OverdueScreenKey()
  }

  private val overdueRecyclerView by bindView<RecyclerView>(R.id.overdue_list)
  private val emptyOverdueListTextView by bindView<TextView>(R.id.overdue_empty_list_text)

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }
  }

  fun setupOverdueList() {
    TODO()
  }
}
