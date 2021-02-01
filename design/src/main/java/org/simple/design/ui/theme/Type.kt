package org.simple.design.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class SimpleTypography(
    val h3: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        letterSpacing = 0.0.sp,
        lineHeight = 56.sp
    ),
    val h4: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 48.sp
    ),
    val h5: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = 0.0.sp,
        lineHeight = 32.sp
    ),
    val h5Numeric: TextStyle = h5.copy(letterSpacing = 1.5.sp),
    val h6: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 28.sp
    ),
    val h6Numeric: TextStyle = h6.copy(letterSpacing = 1.sp),
    val subtitle1: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 24.sp
    ),
    val subtitle1Medium: TextStyle = subtitle1.copy(
        fontWeight = FontWeight.Medium
    ),
    val subtitle2: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 20.sp
    ),
    val body0: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 28.sp
    ),
    val body0Medium: TextStyle = body0.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp
    ),
    val body0Numeric: TextStyle = body0.copy(
        letterSpacing = 2.sp
    ),
    val body0NumericBold: TextStyle = body0Numeric.copy(
        fontWeight = FontWeight.Bold
    ),
    val body1: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 24.sp
    ),
    val body1Numeric: TextStyle = body1.copy(letterSpacing = 1.5.sp),
    val body2: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 20.sp
    ),
    val body2Bold: TextStyle = body2.copy(fontWeight = FontWeight.Bold),
    val body2Numeric: TextStyle = body2.copy(letterSpacing = 1.5.sp),
    val button: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 1.sp,
        lineHeight = 16.sp
    ),
    val buttonBig: TextStyle = button.copy(
        fontSize = 16.sp,
        letterSpacing = 1.25.sp,
        lineHeight = 20.sp
    ),
    val tag: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.8.sp,
        lineHeight = 20.sp
    )
)
