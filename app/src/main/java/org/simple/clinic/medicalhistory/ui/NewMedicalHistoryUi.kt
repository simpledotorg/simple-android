package org.simple.clinic.medicalhistory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
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
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.medicalhistory.newentry.NewMedicalHistoryModel

@Composable
fun NewMedicalHistoryUi(
    model: NewMedicalHistoryModel,
    navigationIconClick: () -> Unit,
    onNextClick: () -> Unit,
    onSelectionChange: (MedicalHistoryQuestion, Answer) -> Unit,
) {
  SimpleTheme {
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
          TopAppBar(
              navigationIcon = {
                IconButton(onClick = navigationIconClick) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
              },
              title = {
                Text(
                    text = model.ongoingPatientEntry?.personalDetails?.fullName ?: "",
                )
              },
          )
        },
        bottomBar = {
          FilledButtonWithFrame(
              testTag = "NEXT_BUTTON",
              onClick = onNextClick,
          ) {
            if (!model.registeringPatient) {
              Text(
                  text = stringResource(R.string.newmedicalhistory_next).uppercase()
              )
            } else {
              CircularProgressIndicator(
                  modifier = Modifier.size(dimensionResource(R.dimen.spacing_24)),
                  color = MaterialTheme.colors.onPrimary
              )
            }

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
        HistoryContainer(
            heartAttackAnswer = model.ongoingMedicalHistoryEntry.hasHadHeartAttack,
            strokeAnswer = model.ongoingMedicalHistoryEntry.hasHadStroke,
            kidneyAnswer = model.ongoingMedicalHistoryEntry.hasHadKidneyDisease,
            diabetesAnswer = model.ongoingMedicalHistoryEntry.hasDiabetes,
            showDiabetesQuestion = !showDiabetesDiagnosis,
            onAnswerChange = onSelectionChange
        )
        if (model.showIsSmokingQuestion) {
          TobaccoContainer(
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
    showIsSmokingQuestion = true,
    showSmokelessTobaccoQuestion = true
)

@Preview
@Composable
private fun NewMedicalHistoryUiPreview() {
  NewMedicalHistoryUi(
      model = previewMedicalHistoryModel,
      navigationIconClick = {},
      onNextClick = {}
  ) { _, _ ->
    //do nothing
  }
}
