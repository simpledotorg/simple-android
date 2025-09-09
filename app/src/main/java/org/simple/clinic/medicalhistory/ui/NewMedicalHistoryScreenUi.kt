package org.simple.clinic.medicalhistory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import org.simple.clinic.common.ui.components.FilledButtonWithFrame
import org.simple.clinic.common.ui.components.TopAppBar
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithDiabetes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnDiabetesTreatment
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnHypertensionTreatment
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.medicalhistory.newentry.NewMedicalHistoryModel

@Composable
fun NewMedicalHistoryScreenUi(
    model: NewMedicalHistoryModel,
    navigationIconClick: () -> Unit,
    onNextClick: () -> Unit,
    onSelectionChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  SimpleTheme {
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SimpleTheme.colors.material.surface,
        topBar = {
          TopAppBar(
              navigationIcon = {
                IconButton(onClick = navigationIconClick) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
              },
              title = {
                Text(
                    text = model.ongoingPatientEntry!!.personalDetails!!.fullName,
                )
              },
          )
        },
        bottomBar = {
          FilledButtonWithFrame(
              testTag = "NEXT_BUTTON",
              onClick = onNextClick,
          ) {
            Text(
                text = stringResource(R.string.newmedicalhistory_next).uppercase()
            )
          }
        },
    ) { paddingValues ->
      val scrollState = rememberScrollState()
      Column(
          modifier = Modifier
                  .fillMaxWidth()
                  .verticalScroll(scrollState)
                  .padding(paddingValues)
                  .padding(dimensionResource(R.dimen.spacing_8)),
          verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
      ) {
        val showDiabetesDiagnosis = model.hasLoadedCurrentFacility && model.facilityDiabetesManagementEnabled
        MedicalHistoryDiagnosisWithTreatment(
            diagnosisLabel = stringResource(R.string.medicalhistory_diagnosis_hypertension_required),
            diagnosisQuestion = DiagnosedWithHypertension,
            diagnosisAnswer = model.ongoingMedicalHistoryEntry.diagnosedWithHypertension,
            treatmentQuestion = IsOnHypertensionTreatment(model.country.isoCountryCode),
            treatmentAnswer = model.ongoingMedicalHistoryEntry.isOnHypertensionTreatment,
            showTreatmentQuestion = model.showOngoingHypertensionTreatment,
            onSelectionChange = onSelectionChange
        )
        if (showDiabetesDiagnosis) {
          MedicalHistoryDiagnosisWithTreatment(
              diagnosisLabel = stringResource(R.string.medicalhistory_diagnosis_diabetes_required),
              diagnosisQuestion = DiagnosedWithDiabetes,
              diagnosisAnswer = model.ongoingMedicalHistoryEntry.hasDiabetes,
              treatmentQuestion = IsOnDiabetesTreatment,
              treatmentAnswer = model.ongoingMedicalHistoryEntry.isOnDiabetesTreatment,
              showTreatmentQuestion = model.showOngoingDiabetesTreatment,
              onSelectionChange = onSelectionChange
          )
        }
        HistoryContainer(
            heartAttackAnswer = model.ongoingMedicalHistoryEntry.hasHadHeartAttack,
            strokeAnswer = model.ongoingMedicalHistoryEntry.hasHadStroke,
            kidneyAnswer = model.ongoingMedicalHistoryEntry.hasHadKidneyDisease,
            diabetesAnswer = model.ongoingMedicalHistoryEntry.hasDiabetes,
            showDiabetesQuestion = !showDiabetesDiagnosis,
            onAnswerChange = onSelectionChange
        )
        if (model.showIsSmokingQuestion) {
          TobaccoQuestion(
              isSmokingAnswer = model.ongoingMedicalHistoryEntry.isSmoking,
              isUsingSmokelessTobaccoAnswer = model.ongoingMedicalHistoryEntry.isUsingSmokelessTobacco,
              showSmokelessTobaccoQuestion = model.showSmokelessTobaccoQuestion,
              onAnswerChange = onSelectionChange
          )
        }
      }
    }
  }
}

private val previewMedicalHistoryModel = NewMedicalHistoryModel(
    country = Country(
        isoCountryCode = "IN",
        displayName = "India",
        isdCode = "91",
        deployments = listOf()
    ),
    ongoingPatientEntry = null,
    ongoingMedicalHistoryEntry = OngoingMedicalHistoryEntry(),
    currentFacility = null,
    nextButtonState = null,
    hasShownChangeDiagnosisError = true,
    showIsSmokingQuestion = true,
    showSmokelessTobaccoQuestion = true
)

@Preview
@Composable
private fun NewMedicalHistoryScreenUiPreview() {
  NewMedicalHistoryScreenUi(
      model = previewMedicalHistoryModel,
      navigationIconClick = {},
      onNextClick = {}
  ) { question, answer ->
    //do nothing
  }
}
