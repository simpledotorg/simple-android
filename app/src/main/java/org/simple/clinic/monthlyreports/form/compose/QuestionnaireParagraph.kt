package org.simple.clinic.monthlyreports.form.compose

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
import org.simple.clinic.questionnaire.component.ParagraphComponentData

@Composable
fun Paragraph(paragraphComponentData: ParagraphComponentData) {
  Text(
      text = paragraphComponentData.text,
      modifier = Modifier.padding(
          top = dimensionResource(id = R.dimen.spacing_8)),
      style = TextStyle(
          color = colorResource(id = R.color.simple_dark_grey),
          fontSize = 16.sp,
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight(400),
          lineHeight = 24.sp,
      )
  )
}
