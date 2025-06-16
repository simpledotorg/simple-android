package org.simple.clinic.summary.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.displayLetterRes
import java.time.Instant
import java.util.UUID

@Composable
fun PatientSummaryToolbar(
  patientName: String,
  gender: Gender,
  age: Int,
  address: String,
  phoneNumber: PatientPhoneNumber?,
  bpPassport: BusinessId?,
  alternativeId: BusinessId?,
  onBack: () -> Unit,
  onContact: () -> Unit,
  onEditPatient: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = SimpleTheme.colors.toolbarPrimary,
    contentColor = SimpleTheme.colors.onToolbarPrimary,
    elevation = AppBarDefaults.TopAppBarElevation,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.statusBarsPadding()
    ) {
      // Actions
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onBack
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = SimpleTheme.colors.onToolbarPrimary,
          )
        }

        Spacer(Modifier.weight(1f))

        if (phoneNumber != null) {
          ToolbarActionTextButton(
            label = phoneNumber.number,
            icon = painterResource(R.drawable.ic_summary_call_icon),
            onClick = onContact,
          )

          Spacer(Modifier.requiredWidth(8.dp))
        }

        ToolbarActionTextButton(
          modifier = Modifier.padding(end = 12.dp),
          label = stringResource(R.string.patientsummary_edit),
          onClick = onEditPatient
        )
      }

      // Content
      PatientInfoContent(
        gender = gender,
        patientName = patientName,
        age = age,
        address = address,
        bpPassport = bpPassport,
        alternativeId = alternativeId
      )
    }
  }
}

@Composable
private fun PatientInfoContent(
  gender: Gender,
  patientName: String,
  age: Int,
  address: String,
  bpPassport: BusinessId?,
  alternativeId: BusinessId?
) {
  val context = LocalContext.current
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(
        start = dimensionResource(id = R.dimen.spacing_16),
        end = dimensionResource(id = R.dimen.spacing_16),
        bottom = dimensionResource(id = R.dimen.spacing_16)
      )
  ) {
    val resources = context.resources
    val genderLetter = resources.getString(gender.displayLetterRes)
    val patientNameGenderAge = resources.getString(
      R.string.patientsummary_toolbar_title,
      patientName,
      genderLetter,
      age.toString()
    )
    Text(
      text = patientNameGenderAge,
      style = MaterialTheme.typography.h6,
      maxLines = 1,
      color = SimpleTheme.colors.onToolbarPrimary,
      modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacing_8))
    )

    Text(
      text = address,
      style = MaterialTheme.typography.body2,
      color = SimpleTheme.colors.onToolbarPrimary72,
      modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacing_4))
    )

    Column(
      modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacing_4))
    ) {
      if (bpPassport != null) {
        Text(
          text = buildBusinessIdString(businessId = bpPassport),
          style = MaterialTheme.typography.body2,
          color = SimpleTheme.colors.onToolbarPrimary72,
          modifier = Modifier.padding(end = dimensionResource(id = R.dimen.spacing_8))
        )
      }

      if (alternativeId != null) {
        Text(
          text = buildBusinessIdString(businessId = alternativeId),
          style = MaterialTheme.typography.body2,
          color = SimpleTheme.colors.onToolbarPrimary72,
          modifier = Modifier.padding(top = if (bpPassport != null) dimensionResource(id = R.dimen.spacing_4) else 0.dp)
        )
      }
    }
  }
}

@Composable
private fun ToolbarActionTextButton(
  label: String,
  modifier: Modifier = Modifier,
  icon: Painter? = null,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .then(modifier)
      .clip(MaterialTheme.shapes.small)
      .clickable { onClick() }
      .background(Color.Black.copy(alpha = 0.24f))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (icon != null) {
      Image(
        painter = icon,
        contentDescription = null,
        modifier = Modifier.requiredSize(16.dp)
      )

      Spacer(Modifier.requiredWidth(4.dp))
    }

    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.button,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      color = MaterialTheme.colors.onPrimary
    )
  }
}

@Composable
private fun buildBusinessIdString(businessId: BusinessId): AnnotatedString {
  val context = LocalContext.current
  val label = businessId.identifier.displayType(context.resources)
  val identifier = businessId.identifier

  return buildAnnotatedString {
    append("$label: ")

    val body2Numeric = SimpleTheme.typography.body2Numeric
    withStyle(
      SpanStyle(
        fontSize = body2Numeric.fontSize,
        letterSpacing = body2Numeric.letterSpacing,
        fontFamily = body2Numeric.fontFamily,
      )
    ) {
      append(identifier.displayValue())
    }
  }
}

@Preview
@Composable
private fun PatientSummaryToolbarPreview() {
  SimpleTheme {
    PatientSummaryToolbar(
      patientName = "SpongeBob",
      gender = Gender.Male,
      age = 25,
      address = "124 Conch Street, Bikini Bottom, Pacific Ocean",
      phoneNumber = PatientPhoneNumber(
        uuid = UUID.fromString("73f2c465-9833-4922-8fb2-6700094fdb3a"),
        patientUuid = UUID.fromString("6878819c-aeb1-4b1f-bcb5-22059697329a"),
        number = "83217387122",
        phoneType = PatientPhoneNumberType.Mobile,
        active = true,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null,
      ),
      bpPassport = BusinessId(
        uuid = UUID.fromString("d758d820-b8d4-448d-be6c-ed2e970d193f"),
        patientUuid = UUID.fromString("4ea1e739-1d5f-4f23-8e73-d42b1445c58e"),
        identifier = Identifier(
          value = "9828b60b-b6d1-4f8a-bcfc-b9ddfc8fc1e2",
          type = Identifier.IdentifierType.BpPassport
        ),
        metaDataVersion = BusinessId.MetaDataVersion.BpPassportMetaDataV1,
        metaData = "",
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null,
        searchHelp = "123456"
      ),
      alternativeId = BusinessId(
        uuid = UUID.fromString("edfcf367-26a7-40a6-8af7-570c718ab387"),
        patientUuid = UUID.fromString("f17756f9-4a45-486e-940d-16cd6c2a2e1b"),
        identifier = Identifier(
          value = "1276417263891739",
          type = Identifier.IdentifierType.BangladeshNationalId
        ),
        metaDataVersion = BusinessId.MetaDataVersion.BangladeshNationalIdMetaDataV1,
        metaData = "",
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null,
        searchHelp = "1276417263891739"
      ),
      onBack = {
        // no-op
      },
      onContact = {
        // no-op
      },
      onEditPatient = {
        // no-op
      }
    )
  }
}
