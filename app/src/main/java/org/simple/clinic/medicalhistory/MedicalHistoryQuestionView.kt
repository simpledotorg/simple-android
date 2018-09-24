package org.simple.clinic.medicalhistory

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.RelativeLayout
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.widgets.UiEvent

@SuppressLint("ClickableViewAccessibility")
class MedicalHistoryQuestionView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  val checkBox by bindView<CheckBox>(R.id.newmedicalhistory_item_checkbox)

  init {
    LayoutInflater.from(context).inflate(R.layout.list_medical_history_question, this, true)
    checkBox.setOnTouchListener { _, event -> this.onTouchEvent(event) }
  }

  fun render(question: MedicalHistoryQuestion) {
    checkBox.setText(question.questionRes)
  }
}
