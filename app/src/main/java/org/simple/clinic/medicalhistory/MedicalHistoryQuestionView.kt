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
import io.reactivex.subjects.PublishSubject
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
  private val divider by bindView<View>(R.id.newmedicalhistory_item_divider)
  private val checkBoxYes by bindView<CompoundButton>(R.id.newmedicalhistory_item_yes)
  private val checkBoxNo by bindView<CompoundButton>(R.id.newmedicalhistory_item_no)
  private val answerSubject = PublishSubject.create<MedicalHistory.Answer>()

  val answers = answerSubject as Observable<MedicalHistory.Answer>

  lateinit var question: MedicalHistoryQuestion

  val color: (Int) -> Int = { colorRes -> ContextCompat.getColor(context, colorRes) }
  val setCheckedState: (CompoundButton) -> Unit = { button ->
    button.apply {
      when {
        isChecked -> {
          setTextColor(color(R.color.white100))
          setCompoundDrawableStart(R.drawable.ic_done_16dp)
          setHorizontalPadding(R.dimen.medicalhistory_unselected_padding)
        }
        else -> {
          setTextColor(color(R.color.blue1))
          setCompoundDrawableStart(null)
          setHorizontalPadding(R.dimen.medicalhistory_selected_padding)
        }
      }
    }
  }

  private val yesListener : (CompoundButton, Boolean) -> Unit = { _ , isChecked ->
    answer = when{
      isChecked -> YES
      else -> UNSELECTED
    }
  }

  private val noListener : (CompoundButton, Boolean) -> Unit = { _ , isChecked ->
    answer = when{
      isChecked -> NO
      else -> UNSELECTED
    }
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.list_medical_history_question2, this, true)
    
    checkBoxYes.setOnCheckedChangeListener(yesListener)
    checkBoxNo.setOnCheckedChangeListener(noListener)

    yesListener(checkBoxYes, false)
    noListener(checkBoxNo, false)
  }

  var answer: MedicalHistory.Answer = UNSELECTED
    set(value) {
      field = value
      answerSubject.onNext(field)
      checkBoxYes.setOnCheckedChangeListener(null)
      checkBoxNo.setOnCheckedChangeListener(null)
      when(value){
        YES -> {
          checkBoxYes.isChecked = true
          checkBoxNo.isChecked = false
        }
        NO -> {
          checkBoxYes.isChecked = false
          checkBoxNo.isChecked = true
        }
        UNSELECTED -> {
          checkBoxYes.isChecked = false
          checkBoxNo.isChecked = false
        }
      }
      checkBoxYes.setOnCheckedChangeListener(yesListener)
      checkBoxNo.setOnCheckedChangeListener(noListener)

      setCheckedState(checkBoxYes)
      setCheckedState(checkBoxNo)
    }

  fun setOnCheckedChangeListener(listener: (View, Boolean) -> Unit) {
   // checkBox.setOnCheckedChangeListener(listener)
  }

  fun hideDivider() {
    divider.visibility = View.GONE
  }

  fun render(question: MedicalHistoryQuestion) {
    this.question = question
    labelTextView.setText(question.questionRes)
  }
}
