package org.simple.clinic.medicalhistory.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion

@Composable
fun DiagnosisContainer(
    hypertensionAnswer: Answer?,
    diabetesAnswer: Answer?,
    showDiabetesDiagnosisView: Boolean,
    modifier: Modifier = Modifier,
    onAnswerChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  Card(modifier = modifier) {
    Column(
        modifier = Modifier
            .padding(horizontal = dimensionResource(R.dimen.spacing_16))
            .padding(
                top = dimensionResource(R.dimen.spacing_16),
                bottom = dimensionResource(R.dimen.spacing_16)
            )
    ) {

      DiagnosisItem(
          header = stringResource(R.string.medicalhistorysummaryview_hypertension_diagnosis),
          question = MedicalHistoryQuestion.DiagnosedWithHypertension,
          selectedAnswer = hypertensionAnswer,
          onAnswerChange = onAnswerChange
      )

      if (showDiabetesDiagnosisView) {

        Spacer(Modifier.requiredHeight(20.dp))

        DiagnosisItem(
            header = stringResource(R.string.medicalhistorysummaryview_diabetes_diagnosis),
            question = MedicalHistoryQuestion.DiagnosedWithDiabetes,
            selectedAnswer = diabetesAnswer,
            onAnswerChange = onAnswerChange
        )
      }
    }
  }
}

@Composable
private fun DiagnosisItem(
    header: String,
    question: MedicalHistoryQuestion,
    selectedAnswer: Answer?,
    onAnswerChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  Column {
    Text(
        text = buildAnnotatedString {
          append(header)
          withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) {
            append(" *")
          }
        },
        style = SimpleTheme.typography.subtitle1Medium.copy(color = MaterialTheme.colors.onSurface),
    )

    Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_4)))

    Row(
        Modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16))
    ) {
      val radioOptions = listOf(Answer.Yes, Answer.No, Answer.Suspected)
      radioOptions.forEach { answer ->
        Row(
            Modifier
                .selectable(
                    selected = (answer == selectedAnswer),
                    onClick = { onAnswerChange(question, answer) },
                    role = Role.RadioButton,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
        ) {
          RadioButton(
              selected = (answer == selectedAnswer),
              onClick = null,
              colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
          )
          Text(
              text = answer.toString(),
              style = MaterialTheme.typography.body1,
              color = MaterialTheme.colors.onSurface,
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DiagnosisItemPreview() {
  SimpleTheme {
    DiagnosisItem(
        header = stringResource(R.string.medicalhistorysummaryview_hypertension_diagnosis),
        question = MedicalHistoryQuestion.DiagnosedWithHypertension,
        selectedAnswer = Answer.Unanswered,
        onAnswerChange = { _, _ ->
          // no-op
        }
    )
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
        modifier = modifier,
        onAnswerChange = { _, _ -> }
    )
  }
}
