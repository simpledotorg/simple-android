package org.simple.clinic.medicalhistory

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.simple.clinic.R
import org.simple.clinic.databinding.ListMedicalHistoryQuestionBinding
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes

@SuppressLint("ClickableViewAccessibility")
class MedicalHistoryQuestionView(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {

  private var binding: ListMedicalHistoryQuestionBinding? = null

  private val contentLayout
    get() = binding!!.contentLayout

  private val yesChip
    get() = binding!!.yesChip

  private val noChip
    get() = binding!!.noChip

  private val dividerView
    get() = binding!!.dividerView

  private val labelTextView
    get() = binding!!.labelTextView

  init {
    val layoutInflater = LayoutInflater.from(context)
    binding = ListMedicalHistoryQuestionBinding.inflate(layoutInflater, this, true)

    val attributes = context.obtainStyledAttributes(attrs, R.styleable.MedicalHistoryQuestionView)
    val contentPaddingStart = attributes.getDimensionPixelSize(R.styleable.MedicalHistoryQuestionView_contentPaddingStart, 0)
    val contentPaddingEnd = attributes.getDimensionPixelSize(R.styleable.MedicalHistoryQuestionView_contentPaddingEnd, 0)
    attributes.recycle()

    contentLayout.setPadding(contentPaddingStart, contentLayout.paddingTop, contentPaddingEnd, contentLayout.paddingBottom)
  }

  private fun updateCheckboxesFromAnswer(answer: Answer) {
    yesChip.isChecked = answer == Yes
    noChip.isChecked = answer == No
  }

  fun hideDivider() {
    dividerView.visibility = View.GONE
  }

  fun showDivider() {
    dividerView.visibility = View.VISIBLE
  }

  fun render(
      question: MedicalHistoryQuestion,
      answer: Answer,
      answerChangeListener: (MedicalHistoryQuestion, Answer) -> Unit
  ) {
    yesChip.setOnCheckedChangeListener(null)
    noChip.setOnCheckedChangeListener(null)

    labelTextView.setText(question.questionRes)
    updateCheckboxesFromAnswer(answer)

    yesChip.setOnCheckedChangeListener { _, checked ->
      val newAnswer = if (checked) Yes else Unanswered
      answerChangeListener.invoke(question, newAnswer)
    }
    noChip.setOnCheckedChangeListener { _, checked ->
      val newAnswer = if (checked) No else Unanswered
      answerChangeListener.invoke(question, newAnswer)
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    binding = null
  }
}
