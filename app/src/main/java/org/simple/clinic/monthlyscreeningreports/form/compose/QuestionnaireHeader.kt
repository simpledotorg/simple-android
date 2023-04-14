package org.simple.clinic.monthlyscreeningreports.form.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.simple.clinic.R
import org.simple.clinic.questionnaire.component.HeaderComponentData
import java.util.Locale

@Composable
fun Header(headerComponentData: HeaderComponentData) {
  Text(
      text = headerComponentData.text.uppercase(Locale.ROOT),
      modifier = Modifier.padding(
          top = dimensionResource(id = R.dimen.spacing_24)),
      style = TextStyle(
          color = colorResource(id = R.color.simple_teal),
          fontSize = 14.sp,
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight(700),
          lineHeight = 24.sp,
      )
  )
}

