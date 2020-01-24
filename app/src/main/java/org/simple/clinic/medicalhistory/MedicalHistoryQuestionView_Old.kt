package org.simple.clinic.medicalhistory

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.widgets.CheckboxWithSuppressibleListener
import org.simple.clinic.widgets.setCompoundDrawableStart
import org.simple.clinic.widgets.setHorizontalPadding
import org.simple.clinic.summary.medicalhistory.MedicalHistorySummaryView

/**
 * This View exists only because it is being used in [MedicalHistorySummaryView]. This will wil be removed
 * once that view is migrated to Mobius.
 *
 * @deprecated Use [MedicalHistoryQuestionView] instead.
 *
 * // TODO(vs): 2020-01-24 Remove this class once [MedicalHistorySummaryView] is migrated to Mobius
 **/
@Deprecated(message = "Use MedicalHistorySummaryView instead")
@SuppressLint("ClickableViewAccessibility")
class MedicalHistoryQuestionView_Old(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  private val labelTextView by bindView<TextView>(R.id.labelTextView)
  private val dividerView by bindView<View>(R.id.dividerView)
  private val yesCheckBox by bindView<CheckboxWithSuppressibleListener>(R.id.yesCheckBox)
  private val noCheckBox by bindView<CheckboxWithSuppressibleListener>(R.id.noCheckBox)
  private val contentLayout by bindView<ViewGroup>(R.id.contentLayout)

  lateinit var question: MedicalHistoryQuestion
  var answerChangeListener: (Answer) -> Unit = {}

  var answer: Answer = Unanswered
    set(value) {
      field = value
      answerChangeListener(value)
      updateCheckboxesFromAnswer()
    }

  private val checkboxChangeListener: (CompoundButton, Boolean) -> Unit = { checkBox, checked ->
    answer = when (checkBox) {
      yesCheckBox -> if (checked) Yes else Unanswered
      noCheckBox -> if (checked) No else Unanswered
      else -> throw AssertionError()
    }
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.list_medical_history_question, this, true)

    val attributes = context.obtainStyledAttributes(attrs, R.styleable.MedicalHistoryQuestionView)
    val contentPaddingStart = attributes.getDimensionPixelSize(R.styleable.MedicalHistoryQuestionView_contentPaddingStart, 0)
    val contentPaddingEnd = attributes.getDimensionPixelSize(R.styleable.MedicalHistoryQuestionView_contentPaddingEnd, 0)
    attributes.recycle()

    contentLayout.setPadding(contentPaddingStart, contentLayout.paddingTop, contentPaddingEnd, contentLayout.paddingBottom)

    yesCheckBox.setOnCheckedChangeListener(checkboxChangeListener)
    noCheckBox.setOnCheckedChangeListener(checkboxChangeListener)

    // Force call the setter.
    answer = answer
  }

  private fun updateCheckboxesFromAnswer() {
    yesCheckBox.runWithoutListener {
      yesCheckBox.isChecked = answer == Yes
    }
    noCheckBox.runWithoutListener {
      noCheckBox.isChecked = answer == No
    }

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

  fun hideDivider() {
    dividerView.visibility = View.GONE
  }

  fun render(question: MedicalHistoryQuestion, answer: Answer) {
    this.question = question
    setAnswerWithoutListener(answer)
    labelTextView.setText(question.questionRes)
  }

  fun answers() = Observable.create<Answer> { emitter ->
    answerChangeListener = emitter::onNext
    emitter.setCancellable { answerChangeListener = {} }

    // Default value.
    emitter.onNext(answer)
  }!!

  private fun setAnswerWithoutListener(answer: Answer) {
    val listenerCopy = answerChangeListener
    answerChangeListener = {}

    this.answer = answer

    answerChangeListener = listenerCopy
  }
}
