package org.simple.clinic.medicalhistory.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion

@Composable
fun DiagnosisContainer(
    hypertensionAnswer: Answer?,
    diabetesAnswer: Answer?,
    showDiabetesDiagnosisView: Boolean,
    showHypertensionSuspectedOption: Boolean,
    showDiabetesSuspectedOption: Boolean,
    modifier: Modifier = Modifier,
    onAnswerChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  Card(modifier = modifier) {
    Column(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.spacing_16))
    ) {

      val hypertensionDiagnosisOptions = if (showHypertensionSuspectedOption) {
        listOf(Answer.Yes, Answer.No, Answer.Suspected)
      } else {
        listOf(Answer.Yes, Answer.No)
      }

      val diabetesDiagnosisOptions = if (showDiabetesSuspectedOption) {
        listOf(Answer.Yes, Answer.No, Answer.Suspected)
      } else {
        listOf(Answer.Yes, Answer.No)
      }

      MedicalHistoryDiagnosisQuestionItem(
          header = stringResource(R.string.medicalhistorysummaryview_hypertension_diagnosis),
          question = MedicalHistoryQuestion.DiagnosedWithHypertension,
          options = hypertensionDiagnosisOptions,
          selectedAnswer = hypertensionAnswer,
          onAnswerChange = onAnswerChange
      )

      if (showDiabetesDiagnosisView) {

        Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_16)))

        Divider(color = SimpleTheme.colors.onSurface11)

        Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_16)))

        MedicalHistoryDiagnosisQuestionItem(
            header = stringResource(R.string.medicalhistorysummaryview_diabetes_diagnosis),
            question = MedicalHistoryQuestion.DiagnosedWithDiabetes,
            options = diabetesDiagnosisOptions,
            selectedAnswer = diabetesAnswer,
            onAnswerChange = onAnswerChange
        )
      }
    }
  }
}

@Preview
@Composable
fun DiagnosisContainerPreview(modifier: Modifier = Modifier) {
  SimpleTheme {
    DiagnosisContainer(
        hypertensionAnswer = Answer.Yes,
        diabetesAnswer = Answer.No,
        showDiabetesDiagnosisView = true,
        showHypertensionSuspectedOption = true,
        showDiabetesSuspectedOption = true,
        modifier = modifier,
        onAnswerChange = { _, _ -> }
    )
  }
}
