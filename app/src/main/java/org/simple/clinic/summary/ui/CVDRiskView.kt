package org.simple.clinic.summary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.cvdrisk.CVDRiskRange


@Composable
fun CVDRiskView(
    modifier: Modifier = Modifier,
    cvdRiskInfo: CVDRiskInfo
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
    ) {
      Text(
          text = stringResource(R.string.medicalhistorysummaryview_cvd_risk),
          style = SimpleTheme.typography.subtitle1Medium,
          color = MaterialTheme.colors.onSurface,
      )
      val riskRange = cvdRiskInfo.cvdRisk
      val secondaryStyle = SpanStyle(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
      val riskText = when {
        cvdRiskInfo.hasCVD ->
          buildAnnotatedString {
            withStyle(secondaryStyle) {
              append(stringResource(R.string.cvd_risk_patient_has_cvd))
            }
          }

        cvdRiskInfo.hasDiabetes ->
          buildAnnotatedString {
            withStyle(secondaryStyle) {
              append(stringResource(R.string.cvd_risk_patient_has_diabetes))
            }
          }

        riskRange != null && riskRange.min == riskRange.max ->
          buildAnnotatedString {
            withStyle(SpanStyle(color = riskRange.level.color)) {
              append(stringResource(riskRange.level.shortLabelResId))
            }

            withStyle(secondaryStyle) {
              append(" ${riskRange.min}%")
            }
          }

        else ->
          buildAnnotatedString {
            withStyle(secondaryStyle) {
              append(stringResource(R.string.cvd_risk_add_smoking_and_cholesterol_info))
            }
          }
      }
      Text(
          modifier = Modifier
              .offset(y = (-dimensionResource(R.dimen.spacing_4)))
              .padding(end = dimensionResource(R.dimen.spacing_16)),
          text = riskText,
          style = MaterialTheme.typography.body1,
          color = MaterialTheme.colors.onSurface,
      )
    }
  }
}

@Preview
@Composable
private fun CVDRiskViewPreview() {
  SimpleTheme {
    CVDRiskView(
        cvdRiskInfo = CVDRiskInfo(
            canShowCVDRisk = true,
            cvdRisk = CVDRiskRange(min = 10, max = 10),
            hasCVD = false,
            hasDiabetes = false
        )
    )
  }
}

@Preview
@Composable
private fun CVDRiskDiabetesViewPreview() {
  SimpleTheme {
    CVDRiskView(
        cvdRiskInfo = CVDRiskInfo(
            canShowCVDRisk = true,
            cvdRisk = CVDRiskRange(min = 10, max = 10),
            hasCVD = false,
            hasDiabetes = true
        )
    )
  }
}

@Preview
@Composable
private fun CVDRiskHeartDiseaseViewPreview() {
  SimpleTheme {
    CVDRiskView(
        cvdRiskInfo = CVDRiskInfo(
            canShowCVDRisk = true,
            cvdRisk = CVDRiskRange(min = 10, max = 10),
            hasCVD = true,
            hasDiabetes = true
        )
    )
  }
}

@Preview
@Composable
private fun CVDRiskAddDataViewPreview() {
  SimpleTheme {
    CVDRiskView(
        cvdRiskInfo = CVDRiskInfo(
            canShowCVDRisk = true,
            cvdRisk = CVDRiskRange(min = 4, max = 14),
            hasCVD = false,
            hasDiabetes = false
        )
    )
  }
}
