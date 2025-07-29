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
    val errorColor = SimpleTheme.colors.material.error
    val subduedColor = SimpleTheme.colors.onSurface67

    if (statinInfo.hasCVD) {
        return StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, errorColor)
    }

    if (statinInfo.hasDiabetes && statinInfo.age >= MIN_AGE_FOR_STATIN) {
        return StatinNudgeDescriptionState(
            R.string.statin_alert_refer_to_doctor_diabetic_40, errorColor
        )
    }

    val highRiskThreshold = if (useVeryHighRiskAsThreshold) {
        CVDRiskLevel.VERY_HIGH
    } else {
        CVDRiskLevel.HIGH
    }

    if (statinInfo.cvdRisk == null || statinInfo.cvdRisk.level >= highRiskThreshold) {
        return StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, errorColor)
    }

    val isSmokingMissing = statinInfo.isSmoker == Answer.Unanswered
    if (isLabBasedStatinNudgeEnabled) {
        val isCholesterolMissing = statinInfo.cholesterol == null
        return when {
            isSmokingMissing && isCholesterolMissing -> StatinNudgeDescriptionState(
                R.string.statin_alert_add_smoking_and_cholesterol_info, subduedColor
            )

            isSmokingMissing -> StatinNudgeDescriptionState(
                R.string.statin_alert_add_smoking_info, subduedColor
            )

            isCholesterolMissing -> StatinNudgeDescriptionState(
                R.string.statin_alert_add_cholesterol_info, subduedColor
            )

            else -> StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, errorColor)
        }
    }

    if (isNonLabBasedStatinNudgeEnabled) {
        val isBmiMissing = statinInfo.bmiReading == null
        return when {
            isSmokingMissing && isBmiMissing -> StatinNudgeDescriptionState(
                R.string.statin_alert_add_smoking_and_bmi_info, subduedColor
            )

            isSmokingMissing -> StatinNudgeDescriptionState(
                R.string.statin_alert_add_smoking_info, subduedColor
            )

            isBmiMissing -> StatinNudgeDescriptionState(
                R.string.statin_alert_add_bmi_info, subduedColor
            )

            else -> StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, errorColor)
        }
    }

    return StatinNudgeDescriptionState(R.string.statin_alert_refer_to_doctor, errorColor)
}

