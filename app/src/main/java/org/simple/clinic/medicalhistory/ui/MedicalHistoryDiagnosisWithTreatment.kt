package org.simple.clinic.medicalhistory.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    modifier: Modifier = Modifier,
    onSelectionChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.spacing_16)),
    ) {
      Text(
          modifier = Modifier
              .padding(bottom = dimensionResource(R.dimen.spacing_12)),
          text = diagnosisLabel,
          style = SimpleTheme.typography.subtitle1Medium,
          color = MaterialTheme.colors.onSurface,
      )

      AnswerChipsGroup(
          modifier = Modifier.align(Alignment.End),
          selectedAnswer = diagnosisAnswer,
          onSelectionChange = { newAnswer ->
            if (newAnswer == diagnosisAnswer) {
              onSelectionChange(diagnosisQuestion, Answer.Unanswered)
            } else {
              onSelectionChange(diagnosisQuestion, newAnswer)
            }
          }
      )

      AnimatedVisibility(
          visible = showTreatmentQuestion
      ) {
        Column {
          Divider(
              modifier = Modifier.padding(
                  top = dimensionResource(R.dimen.spacing_16),
                  bottom = dimensionResource(R.dimen.spacing_8)),
              color = SimpleTheme.colors.onSurface11)

          Text(
              modifier = Modifier
                  .padding(bottom = dimensionResource(R.dimen.spacing_12)),
              text = stringResource(treatmentQuestion.questionRes),
              style = MaterialTheme.typography.body1,
              color = MaterialTheme.colors.onSurface,
          )

          AnswerChipsGroup(
              modifier = Modifier.align(Alignment.End),
              selectedAnswer = treatmentAnswer,
              onSelectionChange = { newAnswer ->
                if (newAnswer == treatmentAnswer) {
                  onSelectionChange(treatmentQuestion, Answer.Unanswered)
                } else {
                  onSelectionChange(treatmentQuestion, newAnswer)
                }
              }
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
        diagnosisLabel = stringResource(R.string.medicalhistory_diagnosis_hypertension_required),
        diagnosisAnswer = Answer.Yes,
        treatmentQuestion = IsOnHypertensionTreatment(Country.INDIA),
        treatmentAnswer = Answer.Unanswered,
        showTreatmentQuestion = true,
        onSelectionChange = { _, _ -> }
    )
  }
}
