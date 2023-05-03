package org.simple.clinic.monthlyreports.form.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import org.simple.clinic.R

@Composable
fun getColor(colorString: String): Color {
  return try {
    Color(android.graphics.Color.parseColor(colorString))
  } catch (_: Throwable) {
    colorResource(id = R.color.simple_dark_grey)
  }
}
