package org.simple.clinic.summary.medicalhistory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.ui.DiagnosisContainer
import org.simple.clinic.medicalhistory.ui.HistoryContainer
import org.simple.clinic.medicalhistory.ui.TobaccoContainer

@Composable
fun MedicalHistorySummary(
    hypertensionAnswer: Answer?,
    diabetesAnswer: Answer?,
    heartAttackAnswer: Answer?,
    strokeAnswer: Answer?,
    kidneyAnswer: Answer?,
    isSmokingAnswer: Answer?,
    isUsingSmokelessTobaccoAnswer: Answer?,
    diabetesManagementEnabled: Boolean,
    showSmokerQuestion: Boolean,
    showSmokelessTobaccoQuestion: Boolean,
    modifier: Modifier = Modifier,
    onAnswerChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  Column(
      modifier = modifier
          .fillMaxWidth()
          .padding(dimensionResource(R.dimen.spacing_8)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
  ) {
    DiagnosisContainer(
        modifier = Modifier.fillMaxWidth(),
        hypertensionAnswer = hypertensionAnswer,
        diabetesAnswer = diabetesAnswer,
        showDiabetesDiagnosisView = diabetesManagementEnabled,
        onAnswerChange = onAnswerChange,
    )

    HistoryContainer(
        heartAttackAnswer = heartAttackAnswer,
        strokeAnswer = strokeAnswer,
        kidneyAnswer = kidneyAnswer,
        diabetesAnswer = diabetesAnswer,
        showDiabetesQuestion = !diabetesManagementEnabled,
        onAnswerChange = onAnswerChange
    )

    if (showSmokerQuestion) {
      TobaccoContainer(
          isSmokingAnswer = isSmokingAnswer,
          isUsingSmokelessTobaccoAnswer = isUsingSmokelessTobaccoAnswer,
          showSmokelessTobaccoQuestion = showSmokelessTobaccoQuestion,
          onAnswerChange = onAnswerChange
      )
    }
  }
}

@Preview
@Composable
private fun MedicalHistorySummaryPreview() {
  SimpleTheme {
    MedicalHistorySummary(
        hypertensionAnswer = Answer.Yes,
        diabetesAnswer = null,
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.No,
        kidneyAnswer = null,
        isSmokingAnswer = null,
        isUsingSmokelessTobaccoAnswer = null,
        diabetesManagementEnabled = true,
        showSmokerQuestion = false,
        showSmokelessTobaccoQuestion = false,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun MedicalHistorySummaryNoDiabetesManagementPreview() {
  SimpleTheme {
    MedicalHistorySummary(
        hypertensionAnswer = Answer.Yes,
        diabetesAnswer = null,
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.No,
        kidneyAnswer = null,
        isSmokingAnswer = null,
        isUsingSmokelessTobaccoAnswer = null,
        diabetesManagementEnabled = false,
        showSmokerQuestion = false,
        showSmokelessTobaccoQuestion = false,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun MedicalHistorySummarySmokerPreview() {
  SimpleTheme {
    MedicalHistorySummary(
        hypertensionAnswer = Answer.Yes,
        diabetesAnswer = null,
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.No,
        kidneyAnswer = null,
        isSmokingAnswer = Answer.Yes,
        isUsingSmokelessTobaccoAnswer = Answer.Yes,
        diabetesManagementEnabled = true,
        showSmokerQuestion = true,
        showSmokelessTobaccoQuestion = false,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
  }
}

@Preview
@Composable
private fun MedicalHistorySummaryTobaccoUsePreview() {
  SimpleTheme {
    MedicalHistorySummary(
        hypertensionAnswer = Answer.Yes,
        diabetesAnswer = null,
        heartAttackAnswer = Answer.Yes,
        strokeAnswer = Answer.No,
        kidneyAnswer = null,
        isSmokingAnswer = Answer.Yes,
        isUsingSmokelessTobaccoAnswer = Answer.Yes,
        diabetesManagementEnabled = true,
        showSmokerQuestion = true,
        showSmokelessTobaccoQuestion = true,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
  }
}
