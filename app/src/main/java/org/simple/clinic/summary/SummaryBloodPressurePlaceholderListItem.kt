package org.simple.clinic.summary

import android.view.View
import android.widget.TextView
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.widgets.UiEvent

data class SummaryBloodPressurePlaceholderListItem(
    private val placeholderNumber: Int,
    private val showHint: Boolean = false
    // We have three placeholders (might change in the future) so we can't hardcode an id. At the same time, we
    // we have to choose IDs that will stop it from clashing with other IDs in the adapter, so we multiply the
    // placeholder number with -20.

    // FIXME: Figure out an automatic solution which will work for all IDs. Also see why we can't use Groupie's
    // auto generated IDs.
) : GroupieItemWithUiEvents<SummaryBloodPressurePlaceholderListItem.BpPlaceholderViewHolder>(adapterId = (placeholderNumber * -20).toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_bp_placeholder

  override fun createViewHolder(itemView: View): BpPlaceholderViewHolder {
    return BpPlaceholderViewHolder(itemView)
  }

  override fun bind(holder: BpPlaceholderViewHolder, position: Int) {
    holder.placeHolderMessageTextView.visibility = if (showHint) View.VISIBLE else View.INVISIBLE
  }

  class BpPlaceholderViewHolder(rootView: View) : ViewHolder(rootView) {
    val placeHolderMessageTextView by bindView<TextView>(R.id.patientsummary_item_bp_placeholder)
  }
}
