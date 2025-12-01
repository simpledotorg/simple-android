package org.simple.clinic.medicalhistory.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnHypertensionTreatment

@Composable
fun MedicalHistoryDiagnosisWithTreatment(
    diagnosisQuestion: MedicalHistoryQuestion,
    diagnosisLabel: String,
    diagnosisAnswer: Answer?,
    treatmentQuestion: MedicalHistoryQuestion,
    treatmentAnswer: Answer?,
    showTreatmentQuestion: Boolean,
    isScreeningFeatureEnabled: Boolean,
    modifier: Modifier = Modifier,
    onSelectionChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  LaunchedEffect(showTreatmentQuestion) {
    if (!showTreatmentQuestion) {
      onSelectionChange(treatmentQuestion, Answer.Unanswered)
    }
  }

  Card(modifier = modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16))
    ) {

      val options = if (isScreeningFeatureEnabled) {
        listOf(Answer.Yes, Answer.No, Answer.Suspected)
      } else {
        listOf(Answer.Yes, Answer.No)
      }

      MedicalHistoryDiagnosisQuestionItem(
          header = diagnosisLabel,
          question = diagnosisQuestion,
          options = options,
          selectedAnswer = diagnosisAnswer,
          onAnswerChange = onSelectionChange
      )

      AnimatedVisibility(
          visible = showTreatmentQuestion
      ) {
        Column {

          Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_16)))

          Divider(color = SimpleTheme.colors.onSurface11)

          Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_16)))

          MedicalHistoryDiagnosisQuestionItem(
              header = stringResource(treatmentQuestion.questionRes),
              question = treatmentQuestion,
              selectedAnswer = treatmentAnswer,
              onAnswerChange = onSelectionChange
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun MedicalHistoryDiagnosisWithTreatmentPreview() {
  SimpleTheme {
    MedicalHistoryDiagnosisWithTreatment(
        diagnosisQuestion = MedicalHistoryQuestion.DiagnosedWithHypertension,
        diagnosisLabel = stringResource(R.string.medicalhistorysummaryview_hypertension_diagnosis),
        diagnosisAnswer = Answer.Yes,
        treatmentQuestion = IsOnHypertensionTreatment(Country.INDIA),
        treatmentAnswer = Answer.Unanswered,
        showTreatmentQuestion = true,
        isScreeningFeatureEnabled = true,
        onSelectionChange = { _, _ -> }
    )
  }
}
