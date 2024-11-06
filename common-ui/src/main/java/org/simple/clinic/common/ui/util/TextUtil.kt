package org.simple.clinic.common.ui.util

import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun htmlToAnnotatedString(htmlText: String) = getSpannedText(htmlText).toAnnotatedString()

private fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
  val spanned = this@toAnnotatedString
  append(spanned.toString())
  getSpans(0, spanned.length, Any::class.java).forEach { span ->
    val start = getSpanStart(span)
    val end = getSpanEnd(span)
    when (span) {
      is StyleSpan -> when (span.style) {
        Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
      }
    }
  }
}

private fun getSpannedText(text: String): Spanned {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
  } else {
    Html.fromHtml(text)
  }
}

