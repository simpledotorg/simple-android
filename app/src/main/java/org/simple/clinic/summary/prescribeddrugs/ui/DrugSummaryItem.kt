package org.simple.clinic.summary.prescribeddrugs.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency

@Composable
fun DrugSummaryItem(
    drugName: String,
    drugDosage: String?,
    drugFrequency: MedicineFrequency?,
    drugDate: String,
    modifier: Modifier = Modifier
) {
  Row(
      modifier = Modifier
          .then(modifier)
          .fillMaxWidth()
          .padding(
              horizontal = dimensionResource(id = R.dimen.spacing_12),
              vertical = dimensionResource(id = R.dimen.spacing_4),
          ),
      verticalAlignment = Alignment.CenterVertically
  ) {
    Image(
        painter = painterResource(id = R.drawable.prescription_drug),
        contentDescription = null,
        modifier = Modifier.size(dimensionResource(id = R.dimen.spacing_16))
    )

    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_12)))

    val drugWithDosageAndFrequency = listOfNotNull(drugName, drugDosage, drugFrequency)
        .joinToString(separator = " ")
    Text(
        modifier = Modifier.weight(1f),
        text = drugWithDosageAndFrequency,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )

    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_8)))

    Text(
        text = drugDate,
        style = MaterialTheme.typography.body2,
        color = SimpleTheme.colors.onSurface67
    )
  }
}

@Preview
@Composable
private fun DrugSummaryItemPreview() {
  SimpleTheme {
    DrugSummaryItem(
        drugName = "Metformin",
        drugDosage = "500 mg",
        drugFrequency = MedicineFrequency.BD,
        drugDate = "20-Feb-2023"
    )
  }
}
