package org.simple.clinic.monthlyReports.questionnaire.entry.epoxy.model

import android.annotation.SuppressLint
import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import org.simple.clinic.R

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass(layout = R.layout.view_questionnaire_line_separator)
abstract class LineSeparatorEpoxyModel : EpoxyModelWithHolder<LineSeparatorEpoxyModel.Holder>() {

  class Holder : EpoxyHolder() {
    override fun bindView(itemView: View) {
    }
  }
}
