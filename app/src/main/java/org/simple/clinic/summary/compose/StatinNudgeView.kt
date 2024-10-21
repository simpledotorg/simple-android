package org.simple.clinic.summary.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun StatinNudge(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
  SimpleTheme {
    AnimatedVisibility(
        visible = isVisible,
    ) {
      Card(
          modifier = modifier
              .fillMaxWidth(),
          shape = RoundedCornerShape(4.dp),
          elevation = CardDefaults.cardElevation(4.dp)
      ) {
        Column(
            modifier = Modifier
                .background(SimpleTheme.colors.onToolbarPrimary)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row {
            Box(
                modifier = Modifier
                    .weight(2f)
            )
            Column(
                modifier = Modifier
                    .weight(3f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                  modifier = Modifier
                      .background(SimpleTheme.colors.error, shape = RoundedCornerShape(50))
                      .padding(horizontal = 8.dp, vertical = 4.dp),
                  style = SimpleTheme.typography.button,
                  color = SimpleTheme.colors.onToolbarPrimary,
                  text = stringResource(R.string.statin_alert_at_risk_patient)
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          RiskProgressBar()
          Spacer(modifier = Modifier.height(16.dp))
          Text(
              text = buildAnnotatedString {
                append(stringResource(R.string.statin_alert_refer_to_doctor_for))
                append(" ")
                withStyle(style = SimpleTheme.typography.body2Bold.toSpanStyle()) {
                  append(stringResource(R.string.statin_alert_statin_medicine))
                }
              },
              color = SimpleTheme.colors.error,
              style = SimpleTheme.typography.body2,
          )
        }
      }
    }
  }
}

@Composable
fun RiskProgressBar() {
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .height(16.dp),
      verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .background(
                Color(0xFF00B849),
                shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
            )
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .background(Color(0xFFFFC800))
    )
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(2.dp)
            .background(Color(0xFF2F363D))
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .background(SimpleTheme.colors.error)
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .background(Color(0xFFB81631))
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .background(Color(0xFF731814))
    )
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(2.dp)
            .background(Color(0xFF2F363D))
    )
  }
}

@Preview
@Composable
fun StatinNudgePreview() {
  StatinNudge(true)
}
