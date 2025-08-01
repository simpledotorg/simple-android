package org.simple.clinic.summary.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.FilledButton
import org.simple.clinic.common.ui.theme.SimpleInverseTheme
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.StatinInfo
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.util.toAnnotatedString

@Composable
fun StatinNudge(
    statinInfo: StatinInfo,
    isNonLabBasedStatinNudgeEnabled: Boolean,
    isLabBasedStatinNudgeEnabled: Boolean,
    useVeryHighRiskAsThreshold: Boolean,
    addTobaccoUseClicked: () -> Unit,
    addBMIClick: () -> Unit,
    addCholesterol: () -> Unit,
    modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
      visible = statinInfo.canShowStatinNudge,
      enter = expandVertically(
          animationSpec = tween(500),
          expandFrom = Alignment.Top
      ),
      exit = shrinkVertically(animationSpec = tween(500))
  ) {
    Card(
        modifier = modifier
    ) {
      BoxWithConstraints(
          modifier = Modifier.padding(16.dp)
      ) {
        val constraints = constraints
        val size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val (startOffset, endOffset) = getOffsets(statinInfo.cvdRisk, size)

        Column {
          RiskText(
              startOffset = startOffset,
              endOffset = endOffset,
              statinInfo = statinInfo,
              parentWidth = constraints.maxWidth,
          )
          Spacer(modifier = Modifier.height(12.dp))
          RiskProgressBar(
              startOffset = startOffset,
              endOffset = endOffset
          )
          Spacer(modifier = Modifier.height(16.dp))
          DescriptionText(
              isLabBasedStatinNudgeEnabled = isLabBasedStatinNudgeEnabled,
              isNonLabBasedStatinNudgeEnabled = isNonLabBasedStatinNudgeEnabled,
              statinInfo = statinInfo,
              useVeryHighRiskAsThreshold = useVeryHighRiskAsThreshold
          )

          val shouldShowButtonsForNonLabNudge = isNonLabBasedStatinNudgeEnabled && !statinInfo.hasDiabetes
          val shouldShowButtonsForLabNudge = isLabBasedStatinNudgeEnabled &&
              statinInfo.cvdRisk?.canPrescribeStatin == true

          val shouldShowAddButtons = !statinInfo.hasCVD &&
              (shouldShowButtonsForNonLabNudge || shouldShowButtonsForLabNudge)

          if (shouldShowAddButtons) {
            StainNudgeAddButtons(
                modifier = Modifier.padding(top = 16.dp),
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = isNonLabBasedStatinNudgeEnabled,
                isLabBasedStatinNudgeEnabled = isLabBasedStatinNudgeEnabled,
                addTobaccoUseClicked = addTobaccoUseClicked,
                addBMIClick = addBMIClick,
                addCholesterol = addCholesterol,
            )
          }
        }
      }
    }
  }
}

@Composable
fun RiskText(
    startOffset: Float,
    endOffset: Float,
    statinInfo: StatinInfo,
    parentWidth: Int,
    parentPadding: Float = 16f
) {
  val midpoint = (startOffset + endOffset) / 2

  val riskPercentage = if (statinInfo.cvdRisk?.min == statinInfo.cvdRisk?.max) {
    "${statinInfo.cvdRisk?.min}%"
  } else {
    "${statinInfo.cvdRisk?.min}-${statinInfo.cvdRisk?.max}%"
  }

  val riskText = when {
    statinInfo.hasCVD -> stringResource(R.string.statin_alert_very_high_risk_patient)

    statinInfo.cvdRisk == null -> stringResource(R.string.statin_alert_at_risk_patient)

    statinInfo.hasDiabetes && !statinInfo.cvdRisk.canPrescribeStatin -> {
      stringResource(R.string.statin_alert_at_risk_patient)
    }

    else -> stringResource(statinInfo.cvdRisk.level.displayStringResId, riskPercentage)
  }

  val riskColor = statinInfo.cvdRisk?.level?.color ?: SimpleTheme.colors.material.error

  val textMeasurer = rememberTextMeasurer()
  val textWidth = textMeasurer.measure(
      text = AnnotatedString(riskText),
      style = SimpleTheme.typography.material.button
  ).size.width
  val totalTextWidth = textWidth + with(LocalDensity.current) { 8.dp.toPx() * 2 }

  val calculatedOffsetX = midpoint - (totalTextWidth / 2)
  val maxOffsetX = parentWidth - totalTextWidth - parentPadding
  val clampedOffsetX = if (maxOffsetX >= 0f) {
    calculatedOffsetX.coerceIn(0f, maxOffsetX)
  } else {
    0f
  }


  Text(
      modifier = Modifier
          .testTag("STATIN_NUDGE_RISK_TEXT")
          .offset {
            IntOffset(
                x = clampedOffsetX.toInt(),
                y = 0
            )
          }
          .background(riskColor, shape = RoundedCornerShape(50))
          .padding(horizontal = 8.dp, vertical = 4.dp),
      style = SimpleTheme.typography.material.button,
      color = SimpleTheme.colors.onToolbarPrimary,
      text = riskText
  )
}

@Composable
fun RiskProgressBar(
    startOffset: Float,
    endOffset: Float
) {
  val riskColors = listOf(
      Color(0xFF00B849), // Low
      Color(0xFFFFC800), // MEDIUM
      SimpleTheme.colors.material.error, // HIGH
      Color(0xFFB81631), // VERY HIGH
      Color(0xFF731814)  // CRITICAL
  )

  val indicatorColor = Color(0xFF2F363D)

  BoxWithConstraints(
      modifier = Modifier
          .fillMaxWidth()
          .height(14.dp)
          .drawWithContent {
            drawContent()

            drawLine(
                color = indicatorColor,
                start = Offset(startOffset, 0f),
                end = Offset(startOffset, size.height),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = indicatorColor,
                start = Offset(endOffset, 0f),
                end = Offset(endOffset, size.height),
                strokeWidth = 2.dp.toPx()
            )
          },
      contentAlignment = Alignment.Center,
  ) {
    val totalSegments = riskColors.size
    val segmentWidthPx = constraints.maxWidth.toFloat() / totalSegments

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(50))
    ) {
      riskColors.forEachIndexed { index, color ->
        val segmentStartPx = index * segmentWidthPx
        val segmentEndPx = (index + 1) * segmentWidthPx

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .drawWithContent {
                  drawRect(color.copy(alpha = 0.5f))

                  val visibleStart = maxOf(segmentStartPx, startOffset)
                  val visibleEnd = minOf(segmentEndPx, endOffset)

                  if (visibleStart < visibleEnd) {
                    drawRect(
                        color = color.copy(alpha = 1.0f),
                        topLeft = Offset(x = visibleStart - segmentStartPx, y = 0f),
                        size = Size(
                            width = visibleEnd - visibleStart,
                            height = size.height
                        )
                    )
                  }
                }
        )
      }
    }
  }
}

@Composable
fun DescriptionText(
    statinInfo: StatinInfo,
    isLabBasedStatinNudgeEnabled: Boolean,
    isNonLabBasedStatinNudgeEnabled: Boolean,
    useVeryHighRiskAsThreshold: Boolean,
) {
  val descriptionState = rememberStatinNudgeDescriptionState(
      statinInfo = statinInfo,
      isLabBasedStatinNudgeEnabled = isLabBasedStatinNudgeEnabled,
      isNonLabBasedStatinNudgeEnabled = isNonLabBasedStatinNudgeEnabled,
      useVeryHighRiskAsThreshold = useVeryHighRiskAsThreshold
  )

  Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = Alignment.Center
  ) {
    Text(
        modifier = Modifier.testTag("STATIN_NUDGE_DESCRIPTION"),
        text = stringResource(id = descriptionState.textResId).toAnnotatedString(),
        color = descriptionState.color,
        style = SimpleTheme.typography.material.body2,
        textAlign = TextAlign.Center,
    )
  }
}

@Composable
fun StainNudgeAddButtons(
    modifier: Modifier,
    statinInfo: StatinInfo,
    isNonLabBasedStatinNudgeEnabled: Boolean,
    isLabBasedStatinNudgeEnabled: Boolean,
    addTobaccoUseClicked: () -> Unit,
    addBMIClick: () -> Unit,
    addCholesterol: () -> Unit,
) {
  SimpleInverseTheme {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (statinInfo.isSmoker == Answer.Unanswered) {
        FilledButton(
            modifier = Modifier
                .testTag("STATIN_NUDGE_ADD_TOBACCO_USE")
                .height(36.dp)
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(50)),
            onClick = { addTobaccoUseClicked.invoke() }
        ) {
          Text(
              text = stringResource(R.string.statin_alert_add_tobacco_use),
              fontSize = 14.sp,
          )
        }
      }

      if (isNonLabBasedStatinNudgeEnabled && statinInfo.bmiReading == null) {
        FilledButton(
            modifier = Modifier
                .testTag("STATIN_NUDGE_ADD_BMI")
                .height(36.dp)
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(50)),
            onClick = { addBMIClick.invoke() }
        ) {
          Text(
              text = stringResource(R.string.statin_alert_add_bmi),
              fontSize = 14.sp,
          )
        }
      }

      if (isLabBasedStatinNudgeEnabled && statinInfo.cholesterol == null) {
        FilledButton(
            modifier = Modifier
                .testTag("STATIN_NUDGE_ADD_CHOLESTEROL")
                .height(36.dp)
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(50)),
            onClick = { addCholesterol() }
        ) {
          Text(
              text = stringResource(R.string.statin_alert_add_cholesterol),
              fontSize = 14.sp,
          )
        }
      }
    }
  }
}

fun getOffsets(cvdRiskRange: CVDRiskRange?, size: Size): Pair<Float, Float> {
  val riskRanges = listOf(
      0..4,    // LOW
      5..9,    // MEDIUM
      10..19,  // HIGH
      20..29,  // VERY HIGH
      30..33 // CRITICAL
  )

  val startRatio: Float
  val endRatio: Float

  if (cvdRiskRange != null) {
    startRatio = riskRanges.findSegmentRatio(cvdRiskRange.min)
    endRatio = riskRanges.findSegmentRatio(cvdRiskRange.max)
  } else {
    startRatio = riskRanges.findSegmentRatio(10)
    endRatio = 1f
  }

  val startOffset = startRatio * size.width
  val endOffset = endRatio * size.width

  return Pair(startOffset, endOffset)
}

fun List<IntRange>.findSegmentRatio(value: Int): Float {
  var accumulatedRatio = 0f
  forEach { range ->
    if (value in range) {
      val rangeFraction = (value - range.first).toFloat() / (range.last - range.first)
      return accumulatedRatio + rangeFraction / size
    }
    accumulatedRatio += 1f / size
  }
  return 1f
}

@Preview
@Composable
fun StatinNudgePreview() {
  SimpleTheme {
    StatinNudge(
        statinInfo = StatinInfo(canShowStatinNudge = true, hasDiabetes = true),
        isNonLabBasedStatinNudgeEnabled = false,
        isLabBasedStatinNudgeEnabled = true,
        addTobaccoUseClicked = {},
        addBMIClick = {},
        addCholesterol = {},
        useVeryHighRiskAsThreshold = false,
    )
  }
}
