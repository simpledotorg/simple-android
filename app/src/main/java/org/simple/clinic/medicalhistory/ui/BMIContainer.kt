package org.simple.clinic.medicalhistory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.ButtonSize
import org.simple.clinic.common.ui.components.TextButton
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.patientattribute.BMIReading

@Composable
fun BMIContainer(
    modifier: Modifier = Modifier,
    bmiReading: BMIReading? = null,
    onAddOrClick: () -> Unit,
) {
  val isBmiRecorded = bmiReading?.calculateBMI() != null
  Card(modifier = modifier) {
    Column(
        modifier = Modifier.padding(
            start = dimensionResource(R.dimen.spacing_16),
            bottom = dimensionResource(R.dimen.spacing_12)
        )
    ) {

      Row(
          modifier = Modifier.padding(
              top = dimensionResource(R.dimen.spacing_4),
              end = dimensionResource(R.dimen.spacing_8)
          ),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
      ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.medicalhistorysummaryview_bmi),
            style = SimpleTheme.typography.subtitle1Medium,
            color = MaterialTheme.colors.onSurface,
        )

        TextButton(
            buttonSize = ButtonSize.ExtraSmall,
            onClick = onAddOrClick
        ) {
          val btnText = if (isBmiRecorded) R.string.bmi_edit else R.string.bmi_add
          Text(
              text = stringResource(btnText).uppercase(),
              style = MaterialTheme.typography.button,
          )
        }
      }

      val bmiText = buildAnnotatedString {
        if (isBmiRecorded) {
          append(bmiReading.calculateBMI().toString())

          append(" ")

          withStyle(
              SpanStyle(
                  color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
              )
          ) {
            append("(${bmiReading.weight.toInt()}kg, ${bmiReading.height.toInt()}cm)")
          }
        } else {

          withStyle(
              SpanStyle(
                  color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
              )
          ) {
            append(stringResource(R.string.bmi_none))
          }
        }
      }
      Text(
          modifier = Modifier
              .offset(y = (-dimensionResource(R.dimen.spacing_4)))
              .padding(end = dimensionResource(R.dimen.spacing_16)),
          text = bmiText,
          style = MaterialTheme.typography.body1,
          color = MaterialTheme.colors.onSurface,
      )
    }
  }
}

@Preview
@Composable
private fun BMINoneContainerPreview() {
  SimpleTheme {
    BMIContainer(
    ) { }
  }
}

@Preview
@Composable
private fun BMIContainerPreview() {
  SimpleTheme {
    BMIContainer(
        bmiReading = BMIReading(height = 150f, weight = 50f)
    ) { }
  }
}
