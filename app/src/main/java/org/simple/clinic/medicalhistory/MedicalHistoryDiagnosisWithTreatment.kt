package org.simple.clinic.medicalhistory

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import org.simple.clinic.databinding.ListMedicalhistoryDiagnosisWithTreatmentBinding

class MedicalHistoryDiagnosisWithTreatment(
    context: Context,
    attrs: AttributeSet?
) : MaterialCardView(context, attrs) {

  private var _binding: ListMedicalhistoryDiagnosisWithTreatmentBinding? = null
  private val binding get() = _binding!!

  private val diagnosisTextView
    get() = binding.diagnosisTextView

  private val diagnosisChipGroup
    get() = binding.diagnosisChipGroup

  private val diagnosisYesChip
    get() = binding.diagnosisYesChip

  private val diagnosisNoChip
    get() = binding.diagnosisNoChip

  private val treatmentQuestion
    get() = binding.treatmentQuestion

  private val treatmentTextView
    get() = binding.treatmentTextView

  private val treatmentChipGroup
    get() = binding.treatmentChipGroup

  private val treatmentYesChip
    get() = binding.treatmentYesChip

  private val treatmentNoChip
    get() = binding.treatmentNoChip

  init {
    val layoutInflater = LayoutInflater.from(context)
    _binding = ListMedicalhistoryDiagnosisWithTreatmentBinding.inflate(layoutInflater, this)
    layoutTransition = LayoutTransition().apply {
      enableTransitionType(LayoutTransition.CHANGING)
      setDuration(LayoutTransition.CHANGING, 150)
    }
  }

  fun showTreatmentQuestion() {
    treatmentQuestion.visibility = VISIBLE
  }

  fun hideTreatmentQuestion() {
    treatmentQuestion.visibility = GONE
  }

  fun clearTreatmentChipGroup() {
    treatmentChipGroup.clearCheck()
  }

  fun renderDiagnosis(
      label: Int,
      question: MedicalHistoryQuestion,
      answer: Answer,
      answerChangeListener: (MedicalHistoryQuestion, Answer) -> Unit
  ) {
    diagnosisChipGroup.setOnCheckedChangeListener(null)

    diagnosisTextView.setText(label)

    updateDiagnosisChipsFromAnswer(answer)

    diagnosisChipGroup.setOnCheckedChangeListener { _, checkedId ->
      val checkedAnswer = when (checkedId) {
        diagnosisYesChip.id -> Answer.Yes
        diagnosisNoChip.id -> Answer.No
        else -> Answer.Unanswered
      }
      answerChangeListener(question, checkedAnswer)
    }
  }

  private fun updateDiagnosisChipsFromAnswer(answer: Answer) {
    diagnosisYesChip.isChecked = answer == Answer.Yes
    diagnosisNoChip.isChecked = answer == Answer.No
  }

  fun renderTreatmentQuestion(
      question: MedicalHistoryQuestion,
      answer: Answer,
      answerChangeListener: (MedicalHistoryQuestion, Answer) -> Unit
  ) {
    treatmentChipGroup.setOnCheckedChangeListener(null)

    treatmentTextView.setText(question.questionRes)

    updateTreatmentChipsFromAnswer(answer)

    treatmentChipGroup.setOnCheckedChangeListener { _, checkedId ->
      val checkedAnswer = when (checkedId) {
        treatmentYesChip.id -> Answer.Yes
        treatmentNoChip.id -> Answer.No
        else -> Answer.Unanswered
      }
      answerChangeListener(question, checkedAnswer)
    }
  }

  private fun updateTreatmentChipsFromAnswer(answer: Answer) {
    treatmentYesChip.isChecked = answer == Answer.Yes
    treatmentNoChip.isChecked = answer == Answer.No
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    _binding = null
  }
}
