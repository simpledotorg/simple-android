package org.simple.clinic.monthlyscreeningreports.form.epoxy.model

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import org.simple.clinic.R
import org.simple.clinic.questionnaire.component.InputViewGroupComponentData

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass(layout = R.layout.view_questionnaire_input_group)
abstract class InputGroupEpoxyModel : EpoxyModelWithHolder<InputGroupEpoxyModel.Holder>() {

  @EpoxyAttribute
  lateinit var inputViewGroupComponentData: InputViewGroupComponentData

  @EpoxyAttribute
  lateinit var action: (Map<String, Any>) -> Unit

  override fun bind(holder: Holder) {
    val items = inputViewGroupComponentData.children
    if (items != null) {
      setLayoutManager(holder.recyclerView, items.count())

      val inputFieldAdapter = InputFieldAdapter {
        action.invoke(it)
      }
      holder.recyclerView.adapter = inputFieldAdapter
      inputFieldAdapter.submitList(items)
    }
  }

  private fun setLayoutManager(recyclerView: RecyclerView, itemCount: Int) {
    recyclerView.layoutManager = GridLayoutManager(
        recyclerView.context,
        if (itemCount > 1) 2 else 1
    )
  }

  class Holder : EpoxyHolder() {
    lateinit var recyclerView: RecyclerView

    override fun bindView(itemView: View) {
      recyclerView = itemView.findViewById(R.id.inputFieldRecyclerView)
    }
  }
}
