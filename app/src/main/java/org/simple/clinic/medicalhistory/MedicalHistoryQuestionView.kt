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

data class MedicalHistoryAnswerToggled(val question: MedicalHistoryQuestion, val selected: Boolean) : UiEvent {
  override val analyticsName = "New Medical History:Answer Toggled"
}

@SuppressLint("ClickableViewAccessibility")
class MedicalHistoryQuestionView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  val checkBox by bindView<CheckBox>(R.id.newmedicalhistory_item_checkbox)
  val toggles = PublishSubject.create<UiEvent>()!!

  lateinit var question: MedicalHistoryQuestion

  init {
    LayoutInflater.from(context).inflate(R.layout.list_medical_history_question, this, true)
    setOnTouchListener { _, event -> checkBox.onTouchEvent(event) }

    checkBox.setOnCheckedChangeListener { _, isChecked ->
      toggles.onNext(MedicalHistoryAnswerToggled(question, isChecked))
    }
  }

  fun render(question: MedicalHistoryQuestion) {
    this.question = question
    checkBox.setText(question.questionRes)
  }
}
