package org.simple.clinic.summary.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.cvdrisk.CVDRiskLevel
import org.simple.clinic.cvdrisk.StatinInfo
import org.simple.clinic.medicalhistory.Answer

private const val MIN_AGE_FOR_STATIN = 40

data class StatinNudgeDescriptionState(
    @StringRes val textResId: Int, val color: Color
)

@Composable
@ReadOnlyComposable
internal fun rememberStatinNudgeDescriptionState(
    isNonLabBasedStatinNudgeEnabled: Boolean,
    isLabBasedStatinNudgeEnabled: Boolean,
    statinInfo: StatinInfo,
    useVeryHighRiskAsThreshold: Boolean,
): StatinNudgeDescriptionState {

  val highRiskThreshold = if (useVeryHighRiskAsThreshold) {
    CVDRiskLevel.VERY_HIGH
  } else {
    CVDRiskLevel.HIGH
  }

  return when {
    statinInfo.hasCVD -> {
      StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, SimpleTheme.colors.material.error)
    }

    statinInfo.hasDiabetes && statinInfo.age >= MIN_AGE_FOR_STATIN -> {
      StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor_diabetic_40, SimpleTheme.colors.material.error)
    }

    statinInfo.cvdRisk == null || statinInfo.cvdRisk.level >= highRiskThreshold -> {
      StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, SimpleTheme.colors.material.error)
    }

    isLabBasedStatinNudgeEnabled -> labBasedDescriptionState(statinInfo)

    isNonLabBasedStatinNudgeEnabled -> nonLabBasedDescriptionState(statinInfo)

    else -> StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, SimpleTheme.colors.material.error)
  }

}

@Composable
@ReadOnlyComposable
private fun labBasedDescriptionState(statinInfo: StatinInfo): StatinNudgeDescriptionState {
  return when {
    statinInfo.isSmoker == Answer.Unanswered && statinInfo.cholesterol == null -> StatinNudgeDescriptionState(
        R.string.statin_alert_add_smoking_and_cholesterol_info, SimpleTheme.colors.onSurface67
    )

    statinInfo.isSmoker == Answer.Unanswered -> StatinNudgeDescriptionState(
        R.string.statin_alert_add_smoking_info, SimpleTheme.colors.onSurface67
    )

    statinInfo.cholesterol == null -> StatinNudgeDescriptionState(
        R.string.statin_alert_add_cholesterol_info, SimpleTheme.colors.onSurface67
    )

    else -> StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, SimpleTheme.colors.material.error)
  }
}

@Composable
@ReadOnlyComposable
private fun nonLabBasedDescriptionState(statinInfo: StatinInfo): StatinNudgeDescriptionState {
  return when {
    statinInfo.isSmoker == Answer.Unanswered && statinInfo.bmiReading == null -> StatinNudgeDescriptionState(
        R.string.statin_alert_add_smoking_and_bmi_info, SimpleTheme.colors.onSurface67
    )

    statinInfo.isSmoker == Answer.Unanswered -> StatinNudgeDescriptionState(
        R.string.statin_alert_add_smoking_info, SimpleTheme.colors.onSurface67
    )

    statinInfo.bmiReading == null -> StatinNudgeDescriptionState(
        R.string.statin_alert_add_bmi_info, SimpleTheme.colors.onSurface67
    )

    else -> StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, SimpleTheme.colors.material.error)
  }
}

