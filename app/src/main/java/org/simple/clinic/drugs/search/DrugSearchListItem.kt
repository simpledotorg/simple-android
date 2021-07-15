package org.simple.clinic.drugs.search

import androidx.paging.PagingData
import androidx.paging.TerminalSeparatorType.SOURCE_COMPLETE
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.shape.ShapeAppearanceModel
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListItemDrugSearchBinding
import org.simple.clinic.databinding.ListItemDrugSearchCornerCapBinding
import org.simple.clinic.drugs.search.DrugSearchListItem.Event
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.dp
import org.simple.clinic.widgets.recyclerview.BindingViewHolder

sealed class DrugSearchListItem : PagingItemAdapter.Item<Event> {

  companion object {

    fun from(searchResults: PagingData<Drug>, searchQuery: String): PagingData<DrugSearchListItem> {
      return searchResults
          .map(::DrugSearchResult)
          .insertSeparators(SOURCE_COMPLETE) { oldItem, newItem ->
            if (oldItem != null && newItem != null) {
              Divider
            } else if (oldItem == null && newItem == null) {
              NewCustomDrug(name = searchQuery)
            } else if (oldItem == null && newItem != null) {
              TopCornerCapItem
            } else if (oldItem != null && newItem == null) {
              BottomCornerCapItem
            } else {
              null
            }
          }
    }
  }

  data class DrugSearchResult(val drug: Drug) : DrugSearchListItem() {

    override fun layoutResId() = R.layout.list_item_drug_search

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListItemDrugSearchBinding

      binding.drugItemCard.setOnClickListener { subject.onNext(Event.DrugClicked(drug)) }

      binding.drugNameTextView.text = drugName(drug)
    }

    private fun drugName(drug: Drug): String {
      return buildString {
        append(drug.name)

        if (!drug.dosage.isNullOrBlank()) {
          append(", ${drug.dosage}")
        }

        if (drug.frequency != null) {
          append(", ${drug.frequency}")
        }
      }
    }
  }

  object Divider : DrugSearchListItem() {

    override fun layoutResId() = R.layout.list_item_drug_search_divider

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {

    }
  }

  data class NewCustomDrug(val name: String) : DrugSearchListItem() {

    override fun layoutResId() = R.layout.list_item_drug_search

    override fun render(
        holder: BindingViewHolder,
        subject: Subject<Event>
    ) {
      val binding = holder.binding as ListItemDrugSearchBinding

      binding.drugItemCard.setOnClickListener { subject.onNext(Event.NewCustomDrugClicked(name)) }

      binding.drugNameTextView.text = name
    }
  }

  object TopCornerCapItem : DrugSearchListItem() {

    override fun layoutResId() = R.layout.list_item_drug_search_corner_cap

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListItemDrugSearchCornerCapBinding
      binding.cardView.shapeAppearanceModel = ShapeAppearanceModel
          .builder()
          .setTopLeftCornerSize(4.dp.toFloat())
          .setTopRightCornerSize(4.dp.toFloat())
          .build()
    }
  }

  object BottomCornerCapItem : DrugSearchListItem() {

    override fun layoutResId() = R.layout.list_item_drug_search_corner_cap

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListItemDrugSearchCornerCapBinding
      binding.cardView.shapeAppearanceModel = ShapeAppearanceModel
          .builder()
          .setBottomLeftCornerSize(4.dp.toFloat())
          .setBottomRightCornerSize(4.dp.toFloat())
          .build()
    }
  }

  sealed class Event {

    data class DrugClicked(val drug: Drug) : Event()

    data class NewCustomDrugClicked(val name: String) : Event()
  }

  class DiffCallback : DiffUtil.ItemCallback<DrugSearchListItem>() {

    override fun areItemsTheSame(
        oldItem: DrugSearchListItem,
        newItem: DrugSearchListItem
    ): Boolean {
      return when {
        oldItem is DrugSearchResult && newItem is DrugSearchResult -> oldItem.drug.id == newItem.drug.id
        oldItem is Divider && newItem is Divider -> false
        oldItem is NewCustomDrug && newItem is NewCustomDrug -> true
        else -> false
      }
    }

    override fun areContentsTheSame(
        oldItem: DrugSearchListItem,
        newItem: DrugSearchListItem
    ): Boolean {
      return oldItem == newItem
    }
  }
}
