package org.simple.clinic.summary.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
    AnimatedVisibility(
        visible = isVisible,
    ) {
        Card(
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
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
                                .background(SimpleTheme.colors.material.error, shape = RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = SimpleTheme.typography.material.button,
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
                    color = SimpleTheme.colors.material.error,
                    style = SimpleTheme.typography.material.body2,
                )
            }
        }
    }
}

@Composable
fun RiskProgressBar() {
    val riskColors = listOf(
        Color(0xFF00B849), // Very Low
        Color(0xFFFFC800), // Low
        SimpleTheme.colors.material.error, // Medium
        Color(0xFFB81631), // High
        Color(0xFF731814)  // Very High
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .drawWithContent {
                drawContent()

                val widthPerSegment = size.width / riskColors.size

                drawLine(
                    color = Color(0xFF2F363D),
                    start = Offset(2 * widthPerSegment, 0f),
                    end = Offset(2 * widthPerSegment, size.height),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF2F363D),
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            riskColors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color,
                            shape = when (index) {
                                0 -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                                riskColors.size - 1 -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                                else -> RoundedCornerShape(0)
                            }
                        )

                )
            }
        }
    }
}

@Preview
@Composable
fun StatinNudgePreview() {
    SimpleTheme {
        StatinNudge(true)
    }
}
