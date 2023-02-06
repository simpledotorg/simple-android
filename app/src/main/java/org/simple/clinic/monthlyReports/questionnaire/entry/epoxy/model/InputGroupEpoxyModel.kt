package org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import org.simple.clinic.R
import org.simple.clinic.monthlyReports.questionnaire.component.InputViewGroupComponentData

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass(layout = R.layout.view_questionnaire_input_group)
abstract class InputGroupEpoxyModel : EpoxyModelWithHolder<InputGroupEpoxyModel.Holder>() {

  @EpoxyAttribute
  lateinit var inputViewGroupComponentData: InputViewGroupComponentData

  override fun bind(holder: Holder) {
    val items = inputViewGroupComponentData.children
    if (items != null) {
      val inputFieldAdapter = InputFieldAdapter()
      holder.recyclerView.layoutManager = GridLayoutManager(
          holder.recyclerView.context,
          if (items.count() > 1) 2 else 1
      )
      holder.recyclerView.adapter = inputFieldAdapter
      inputFieldAdapter.submitList(items)
    }
  }

  class Holder : EpoxyHolder() {
    lateinit var recyclerView: RecyclerView

    override fun bindView(itemView: View) {
      recyclerView = itemView.findViewById(R.id.inputFieldRecyclerView)
    }
  }
}
