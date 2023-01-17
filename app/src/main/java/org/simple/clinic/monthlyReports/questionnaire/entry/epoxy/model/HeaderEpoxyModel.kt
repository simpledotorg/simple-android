package org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import org.simple.clinic.R

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass(layout = R.layout.view_questionnaire_header)
abstract class HeaderEpoxyModel : EpoxyModelWithHolder<HeaderEpoxyModel.HeaderEpoxyHolder>() {

  @EpoxyAttribute
  lateinit var title: String

  override fun bind(holder: HeaderEpoxyHolder) {
    holder.titleView.text = title
  }

  class HeaderEpoxyHolder : EpoxyHolder() {

    lateinit var titleView: TextView

    override fun bindView(itemView: View) {
      titleView = itemView.findViewById(R.id.headerTextView)
    }
  }
}

