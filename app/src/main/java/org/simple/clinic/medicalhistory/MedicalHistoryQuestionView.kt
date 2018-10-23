package org.simple.clinic.medicalhistory

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.TextView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.NO
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.UNSELECTED
import org.simple.clinic.medicalhistory.MedicalHistory.Answer.YES
import org.simple.clinic.widgets.setCompoundDrawableStart
import org.simple.clinic.widgets.setHorizontalPadding

@SuppressLint("ClickableViewAccessibility")
class MedicalHistoryQuestionView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  private val labelTextView by bindView<TextView>(R.id.newmedicalhistory_item_label)
  private val dividerView by bindView<View>(R.id.newmedicalhistory_item_divider)
  private val yesCheckBox by bindView<CompoundButton>(R.id.newmedicalhistory_item_yes)
  private val noCheckBox by bindView<CompoundButton>(R.id.newmedicalhistory_item_no)

  lateinit var question: MedicalHistoryQuestion
  var answerChangeListener: (MedicalHistory.Answer) -> Unit = {}

  var answer: MedicalHistory.Answer = UNSELECTED
    set(value) {
      field = value
      answerChangeListener(value)
      updateCheckboxesFromAnswer()
    }

  private val checkboxChangeListener: (CompoundButton, Boolean) -> Unit = { checkBox, checked ->
    answer = when {
      checkBox == yesCheckBox && checked -> YES
      checkBox == noCheckBox && checked -> NO
      else -> UNSELECTED
    }
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.list_medical_history_question, this, true)

    yesCheckBox.setOnCheckedChangeListener(checkboxChangeListener)
    noCheckBox.setOnCheckedChangeListener(checkboxChangeListener)

    // Force call the setter.
    answer = answer
  }

  private fun updateCheckboxesFromAnswer() {
    yesCheckBox.setOnCheckedChangeListener(null)
    noCheckBox.setOnCheckedChangeListener(null)

    yesCheckBox.isChecked = answer == YES
    noCheckBox.isChecked = answer == NO

    yesCheckBox.setOnCheckedChangeListener(checkboxChangeListener)
    noCheckBox.setOnCheckedChangeListener(checkboxChangeListener)

    arrayOf(yesCheckBox, noCheckBox).forEach { checkBox ->
      checkBox.run {
        val color: (Int) -> Int = { colorRes -> ContextCompat.getColor(context, colorRes) }

        when {
          isChecked -> {
            setTextColor(color(R.color.white100))
            setCompoundDrawableStart(R.drawable.ic_done_16dp)
            setHorizontalPadding(R.dimen.medicalhistory_selected_padding)
          }
          else -> {
            setTextColor(color(R.color.blue1))
            setCompoundDrawableStart(null)
            setHorizontalPadding(R.dimen.medicalhistory_unselected_padding)
          }
        }
      }
    }
  }

  // FIXME
  fun setOnCheckedChangeListener(listener: (View, Boolean) -> Unit) {
    //     yesCheckBox.setOnCheckedChangeListener(listener)
    //     noCheckBox.setOnCheckedChangeListener(listener)
  }

  fun hideDivider() {
    dividerView.visibility = View.GONE
  }

  // TODO: Add answer
  fun render(question: MedicalHistoryQuestion) {
    this.question = question
    labelTextView.setText(question.questionRes)
  }

  fun answers() = Observable.create<MedicalHistory.Answer> { emitter ->
    answerChangeListener = emitter::onNext
    emitter.onNext(answer)
    emitter.setCancellable { answerChangeListener = {} }
  }!!
}
