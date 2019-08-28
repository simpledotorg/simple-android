package org.simple.clinic.util.identifierdisplay

import android.content.Context
import org.simple.clinic.R
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode

fun formatShortCode(shortCode: String): String {
  val prefix = shortCode.substring(0, 3)
  val suffix = shortCode.substring(3)

  return "$prefix${Unicode.nonBreakingSpace}$suffix"
}

private fun formatShortCodeForDisplay(
    textSpan: TextAppearanceWithLetterSpacingSpan,
    shortCode: String
): CharSequence {
  val formattedShortCode = formatShortCode(shortCode)
  return Truss()
      .pushSpan(textSpan)
      .append(formattedShortCode)
      .popSpan()
      .build()

}

fun formatShortCodeForDisplay(context: Context, shortCode: String): CharSequence {
  val textSpacingSpan = TextAppearanceWithLetterSpacingSpan(context, R.style.Clinic_V2_TextAppearance_Body0Left_Numeric_White100)
  return formatShortCodeForDisplay(textSpacingSpan, shortCode)
}
