package org.simple.clinic.monthlyscreeningreports.form.epoxy.model

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import org.simple.clinic.R

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass(layout = R.layout.view_questionnaire_sub_header)
abstract class SubHeaderEpoxyModel : EpoxyModelWithHolder<SubHeaderEpoxyModel.Holder>() {

  @EpoxyAttribute
  lateinit var title: String

  override fun bind(holder: Holder) {
    holder.titleView.text = title
  }

  class Holder : EpoxyHolder() {

    lateinit var titleView: TextView

    override fun bindView(itemView: View) {
      titleView = itemView.findViewById(R.id.subHeaderTextView)
    }
  }
}

