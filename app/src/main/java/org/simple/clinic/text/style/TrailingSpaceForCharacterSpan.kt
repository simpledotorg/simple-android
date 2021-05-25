package org.simple.clinic.text.style

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan
import kotlin.math.roundToInt

private const val SPACE = ' '

class TrailingSpaceForCharacterSpan : ReplacementSpan() {
  override fun getSize(
      paint: Paint,
      text: CharSequence?,
      start: Int,
      end: Int,
      fm: Paint.FontMetricsInt?
  ): Int {
    text ?: return 0
    return paint.measureText("${text.substring(start, end)}$SPACE").roundToInt()
  }

  override fun draw(
      canvas: Canvas,
      text: CharSequence?,
      start: Int,
      end: Int,
      x: Float,
      top: Int,
      y: Int,
      bottom: Int,
      paint: Paint
  ) {
    text ?: return
    canvas.drawText(text, start, end, x, y.toFloat(), paint)
  }
}
