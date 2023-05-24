package org.simple.clinic.monthlyreports.form.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import org.simple.clinic.R

@Composable
fun QuestionnaireRadioButton(
    isSelected: Boolean,
    onClick: (String) -> Unit,
    text: String,
) {
  Row(
      modifier = Modifier
          .selectable(
              selected = isSelected,
              onClick = { onClick(text) },
              role = Role.RadioButton
          )
          .padding(all = dimensionResource(id = R.dimen.spacing_14)),
      verticalAlignment = Alignment.CenterVertically
  ) {
    RadioButton(
        selected = isSelected,
        onClick = null,
        colors = RadioButtonDefaults.colors(
            selectedColor = colorResource(id = R.color.simple_light_blue_500),
            unselectedColor = colorResource(id = R.color.color_on_surface_67)
        )
    )
    Text(
        modifier = Modifier.padding(
            start = dimensionResource(id = R.dimen.spacing_14)),
        text = text,
        style = TextStyle(
            color = colorResource(id = if (isSelected) R.color.simple_dark_grey else R.color.color_on_surface_67),
            fontSize = 14.sp,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 20.sp,
        )
    )
  }
}
