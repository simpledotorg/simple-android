package org.simple.clinic.summary.prescribeddrugs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.ButtonSize
import org.simple.clinic.common.ui.components.TextButton
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.util.RealUserClock
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun DrugSummary(
    prescribedDrugs: List<PrescribedDrug>,
    drugDateFormatter: DateTimeFormatter,
    userClock: UserClock,
    onEditMedicinesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colors.surface)
          .then(modifier)
  ) {
    TextButton(
        leadingIcon = {
          val icon = if (prescribedDrugs.isNotEmpty()) {
            painterResource(id = R.drawable.ic_edit_medicine)
          } else {
            painterResource(id = R.drawable.ic_add_circle_blue1_24dp)
          }

          Icon(
              painter = icon,
              contentDescription = null
          )
        },
        onClick = onEditMedicinesClick,
        buttonSize = ButtonSize.Small,
    ) {
      val label = if (prescribedDrugs.isNotEmpty()) {
        stringResource(id = R.string.patientsummary_prescriptions_update)
      } else {
        stringResource(id = R.string.patientsummary_prescriptions_add)
      }.uppercase()

      Text(text = label)
    }

    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.spacing_8)),
        thickness = 1.dp,
        color = SimpleTheme.colors.onSurface11,
    )

    if (prescribedDrugs.isNotEmpty()) {
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(top = dimensionResource(id = R.dimen.spacing_4))
      ) {
        prescribedDrugs.forEach { prescribedDrug ->
          DrugSummaryItem(
              drugName = prescribedDrug.name,
              drugDosage = prescribedDrug.dosage,
              drugFrequency = prescribedDrug.frequency,
              drugDate = drugDateFormatter.format(prescribedDrug.updatedAt.toLocalDateAtZone(userClock.zone))
          )
        }
      }
    } else {
      Text(
          text = stringResource(id = R.string.drugsummaryview_no_medicines),
          modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = dimensionResource(id = R.dimen.spacing_16)),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.body1,
          color = SimpleTheme.colors.onSurface67
      )
    }

    when {
      prescribedDrugs.size > 1 -> {
        Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_8)))
      }

      prescribedDrugs.isNotEmpty() -> {
        Spacer(Modifier.requiredHeight(dimensionResource(R.dimen.spacing_24)))
      }

      else -> {
        // no-op
      }
    }
  }
}

@Preview
@Composable
private fun DrugSummaryPreviewEmpty() {
  SimpleTheme {
    DrugSummary(
        prescribedDrugs = emptyList(),
        drugDateFormatter = DateTimeFormatter.ISO_DATE,
        userClock = RealUserClock(ZoneId.systemDefault()),
        onEditMedicinesClick = {
          // no-op
        },
    )
  }
}

@Preview
@Composable
private fun DrugSummaryPreviewWithContent() {
  SimpleTheme {
    DrugSummary(
        prescribedDrugs = listOf(
            PrescribedDrug(
                uuid = UUID.fromString("39173872-ce0b-4b9d-a629-c54cc9b0a2e9"),
                name = "Metaforming 500mg BD",
                dosage = "500 mg",
                rxNormCode = null,
                isDeleted = false,
                isProtocolDrug = true,
                patientUuid = UUID.fromString("519bc844-1120-4265-b5b8-ba4f1340ed04"),
                facilityUuid = UUID.fromString("4fe631a2-b860-40f3-be1f-cd5202fc5411"),
                syncStatus = SyncStatus.DONE,
                timestamps = Timestamps(
                    createdAt = Instant.parse("2018-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
                    deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
                ),
                frequency = null,
                durationInDays = null,
                teleconsultationId = null,
            )
        ),
        drugDateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
        userClock = RealUserClock(ZoneId.systemDefault()),
        onEditMedicinesClick = {
          // no-op
        },
    )
  }
}
