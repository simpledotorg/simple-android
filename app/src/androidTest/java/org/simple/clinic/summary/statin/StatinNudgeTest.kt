package org.simple.clinic.summary.statin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.core.text.HtmlCompat
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.R
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.StatinInfo
import org.simple.clinic.summary.compose.StatinNudge

class StatinNudgeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun whenPatientHasCVD_itShouldDisplayTheAbsoluteHighRiskState() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            hasCVD = true,
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = true,
                isLabBasedStatinNudgeEnabled = false,
                useVeryHighRiskAsThreshold = true,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(context.getString(R.string.statin_alert_very_high_risk_patient))

        val referToDoctorString = context.getString(R.string.statin_alert_refer_to_doctor)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        hideButtons()
    }

    @Test
    fun whenNonLabBasedIsEnabled_andPatientAgedAbove40HasDiabetes_itShouldDisplayTheAbsoluteRiskState() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            hasDiabetes = true,
            age = 42
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = true,
                isLabBasedStatinNudgeEnabled = false,
                useVeryHighRiskAsThreshold = true,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(context.getString(R.string.statin_alert_at_risk_patient))

        val referToDoctorString =
            context.getString(R.string.statin_alert_refer_to_doctor_diabetic_40)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        hideButtons()
    }

    @Test
    fun whenNonLabBasedIsEnabled_andPatientHasLowRisk_itShouldShowLowRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(3, 10)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = true,
                isLabBasedStatinNudgeEnabled = false,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_low_high_risk_patient_x,
                    "3-10%"
                )
            )

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(context.getString(R.string.statin_alert_add_smoking_and_bmi_info))

        showSmokingAndBmiButtons()
    }

    @Test
    fun whenNonLabBasedIsEnabled_andPatientHasMediumRisk_itShouldShowMediumRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(7, 14)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = true,
                isLabBasedStatinNudgeEnabled = false,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_medium_high_risk_patient_x,
                    "7-14%"
                )
            )

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(context.getString(R.string.statin_alert_add_smoking_and_bmi_info))

        showSmokingAndBmiButtons()
    }

    @Test
    fun whenNonLabBasedIsEnabled_andPatientHasHighRisk_itShouldShowHighRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(11, 21)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = true,
                isLabBasedStatinNudgeEnabled = false,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_high_risk_patient_x,
                    "11-21%"
                )
            )

        val referToDoctorString = context.getString(R.string.statin_alert_refer_to_doctor)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        showSmokingAndBmiButtons()
    }

    @Test
    fun whenNonLabBasedAndHighRiskThresholdIsEnabled_andPatientHasHighRisk_itShouldShowHighRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(11, 21)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = true,
                isLabBasedStatinNudgeEnabled = false,
                useVeryHighRiskAsThreshold = true,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_high_risk_patient_x,
                    "11-21%"
                )
            )

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(context.getString(R.string.statin_alert_add_smoking_and_bmi_info))

        showSmokingAndBmiButtons()
    }

    @Test
    fun whenNonLabBasedIsEnabled_andPatientHasVeryHighRisk_itShouldShowVeryHighRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(22, 31)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = true,
                isLabBasedStatinNudgeEnabled = false,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_very_high_risk_range,
                    "22-31%"
                )
            )

        val referToDoctorString = context.getString(R.string.statin_alert_refer_to_doctor)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        showSmokingAndBmiButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientAgedAbove74HasDiabetes_itShouldDisplayTheAbsoluteRiskState() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            hasDiabetes = true,
            age = 78,
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(context.getString(R.string.statin_alert_at_risk_patient))

        val referToDoctorString =
            context.getString(R.string.statin_alert_refer_to_doctor_diabetic_40)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        hideButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientAgedBelow74HasDiabetesWithRiskLessThan10_itShouldShowAbsoluteRiskState() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            hasDiabetes = true,
            age = 66,
            cvdRisk = CVDRiskRange(2, 8)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(context.getString(R.string.statin_alert_at_risk_patient))

        val referToDoctorString =
            context.getString(R.string.statin_alert_refer_to_doctor_diabetic_40)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        hideButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientAgedBelow74HasDiabetesWithLowRisk_itShouldShowLowRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            hasDiabetes = true,
            age = 66,
            cvdRisk = CVDRiskRange(3, 10)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_low_high_risk_patient_x,
                    "3-10%"
                )
            )

        val referToDoctorString =
            context.getString(R.string.statin_alert_refer_to_doctor_diabetic_40)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        showSmokingAndCholesterolButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientAgedBelow74HasDiabetesWithMediumRisk_itShouldShowMediumRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            hasDiabetes = true,
            age = 66,
            cvdRisk = CVDRiskRange(7, 14)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_medium_high_risk_patient_x,
                    "7-14%"
                )
            )

        val referToDoctorString =
            context.getString(R.string.statin_alert_refer_to_doctor_diabetic_40)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        showSmokingAndCholesterolButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientAgedBelow74HasDiabetesWithHighRisk_itShouldShowHighRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            hasDiabetes = true,
            age = 66,
            cvdRisk = CVDRiskRange(12, 21)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_high_risk_patient_x,
                    "12-21%"
                )
            )

        val referToDoctorString =
            context.getString(R.string.statin_alert_refer_to_doctor_diabetic_40)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        showSmokingAndCholesterolButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientAgedBelow74HasDiabetesWithVeryHighRisk_itShouldShowVeryHighRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            hasDiabetes = true,
            age = 66,
            cvdRisk = CVDRiskRange(21, 33)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_very_high_risk_range,
                    "21-33%"
                )
            )

        val referToDoctorString =
            context.getString(R.string.statin_alert_refer_to_doctor_diabetic_40)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        showSmokingAndCholesterolButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientHasLowRisk_itShouldShowLowRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(3, 10)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_low_high_risk_patient_x,
                    "3-10%"
                )
            )

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(context.getString(R.string.statin_alert_add_smoking_and_cholesterol_info))

        showSmokingAndCholesterolButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientHasMediumRisk_itShouldShowMediumRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(7, 14)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_medium_high_risk_patient_x,
                    "7-14%"
                )
            )

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(context.getString(R.string.statin_alert_add_smoking_and_cholesterol_info))

        showSmokingAndCholesterolButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientHasHighRisk_itShouldShowHighRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(11, 21)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_high_risk_patient_x,
                    "11-21%"
                )
            )

        val referToDoctorString = context.getString(R.string.statin_alert_refer_to_doctor)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        showSmokingAndCholesterolButtons()
    }

    @Test
    fun whenLabBasedAndHighRiskThresholdIsEnabled_andPatientHasHighRisk_itShouldShowHighRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(11, 21)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = true,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_high_risk_patient_x,
                    "11-21%"
                )
            )

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(context.getString(R.string.statin_alert_add_smoking_and_cholesterol_info))

        showSmokingAndCholesterolButtons()
    }

    @Test
    fun whenLabBasedIsEnabled_andPatientHasVeryHighRisk_itShouldShowVeryHighRiskRange() {
        //given
        val statinInfo = StatinInfo(
            canShowStatinNudge = true,
            age = 48,
            cvdRisk = CVDRiskRange(22, 31)
        )

        //when
        composeTestRule.setContent {
            StatinNudge(
                statinInfo = statinInfo,
                isNonLabBasedStatinNudgeEnabled = false,
                isLabBasedStatinNudgeEnabled = true,
                useVeryHighRiskAsThreshold = false,
                addSmokingClick = { },
                addBMIClick = { },
                addCholesterol = { }
            )
        }

        //then
        composeTestRule.onNodeWithTag("STATIN_NUDGE_RISK_TEXT")
            .assertTextEquals(
                context.getString(
                    R.string.statin_alert_very_high_risk_range,
                    "22-31%"
                )
            )

        val referToDoctorString = context.getString(R.string.statin_alert_refer_to_doctor)
        val expectedDescriptionText =
            HtmlCompat.fromHtml(referToDoctorString, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_DESCRIPTION")
            .assertTextEquals(expectedDescriptionText)

        showSmokingAndCholesterolButtons()
    }

    private fun hideButtons() {
        composeTestRule.onNodeWithTag("STATIN_NUDGE_ADD_SMOKING")
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_ADD_BMI")
            .assertDoesNotExist()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_ADD_CHOLESTEROL")
            .assertDoesNotExist()
    }

    private fun showSmokingAndBmiButtons() {
        composeTestRule.onNodeWithTag("STATIN_NUDGE_ADD_SMOKING")
            .assertTextEquals(context.getString(R.string.statin_alert_add_smoking))
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_ADD_BMI")
            .assertTextEquals(context.getString(R.string.statin_alert_add_bmi))
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_ADD_CHOLESTEROL")
            .assertDoesNotExist()
    }

    private fun showSmokingAndCholesterolButtons() {
        composeTestRule.onNodeWithTag("STATIN_NUDGE_ADD_SMOKING")
            .assertTextEquals(context.getString(R.string.statin_alert_add_smoking))
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_ADD_CHOLESTEROL")
            .assertTextEquals(context.getString(R.string.statin_alert_add_cholesterol))
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("STATIN_NUDGE_ADD_BMI")
            .assertDoesNotExist()
    }
}
