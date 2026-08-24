package org.simple.clinic.widgets.ageanddateofbirth

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.widgets.setTextWithWatcher

class DateOfBirthEditTextTest {

  private lateinit var editText: DateOfBirthEditText

  @Before
  fun setUp() {
    val context = InstrumentationRegistry
        .getInstrumentation()
        .targetContext

    editText = DateOfBirthEditText(
        context = context,
        attrs = null
    )
  }

  @Test
  fun typingOneDigit_keepsOneDigit() {
    editText.setText("1")

    assertThat(editText.text.toString()).isEqualTo("1")
  }

  @Test
  fun typingTwoDigits_doesNotAddSlashUntilNextDigit() {
    editText.setText("12")

    assertThat(editText.text.toString()).isEqualTo("12")
  }

  @Test
  fun typingThreeDigits_addsFirstSlash() {
    editText.setText("123")

    assertThat(editText.text.toString()).isEqualTo("12/3")
  }

  @Test
  fun typingFourDigits_formatsDayAndMonth() {
    editText.setText("1204")

    assertThat(editText.text.toString()).isEqualTo("12/04")
  }

  @Test
  fun typingFiveDigits_addsSecondSlash() {
    editText.setText("12041")

    assertThat(editText.text.toString()).isEqualTo("12/04/1")
  }

  @Test
  fun typingEightDigits_formatsCompleteDate() {
    editText.setText("12041995")

    assertThat(editText.text.toString()).isEqualTo("12/04/1995")
  }

  @Test
  fun moreThanEightDigits_areIgnored() {
    editText.setText("120419951234")

    assertThat(editText.text.toString()).isEqualTo("12/04/1995")
  }

  @Test
  fun nonNumericCharacters_areRemoved() {
    editText.setText("12ab04cd1995")

    assertThat(editText.text.toString()).isEqualTo("12/04/1995")
  }

  @Test
  fun existingSlashes_areNotDuplicated() {
    editText.setText("12/04/1995")

    assertThat(editText.text.toString()).isEqualTo("12/04/1995")
  }

  @Test
  fun mixedFormattedAndUnformattedInput_isNormalized() {
    editText.setText("12/041995")

    assertThat(editText.text.toString()).isEqualTo("12/04/1995")
  }

  @Test
  fun setText_withUnformattedDate_formatsDate() {
    editText.setText("19111965")

    assertThat(editText.text.toString()).isEqualTo("19/11/1965")
  }

  @Test
  fun setText_withFormattedDate_preservesDate() {
    editText.setText("19/11/1965")

    assertThat(editText.text.toString()).isEqualTo("19/11/1965")
  }

  @Test
  fun setText_withEmptyString_clearsField() {
    editText.setText("19/11/1965")

    editText.setText("")

    assertThat(editText.text.toString()).isEmpty()
  }

  @Test
  fun setText_afterClearing_canEnterNewDate() {
    editText.setText("19/11/1965")
    editText.setText("")
    editText.setText("10041995")

    assertThat(editText.text.toString()).isEqualTo("10/04/1995")
  }

  @Test
  fun setSelection_canPlaceCursorAtEndOfCompleteDate() {
    editText.setText("19111965")
    editText.setSelection(editText.length())

    assertThat(editText.selectionStart).isEqualTo(10)
    assertThat(editText.selectionEnd).isEqualTo(10)
  }

  @Test
  fun cursorAtBeginning_canBeSet() {
    editText.setText("19/11/1965")

    editText.setSelection(0)

    assertThat(editText.selectionStart).isEqualTo(0)
    assertThat(editText.selectionEnd).isEqualTo(0)
  }

  @Test
  fun cursorCanBePlacedBeforeYear() {
    editText.setText("19/11/1965")

    editText.setSelection(6)

    assertThat(editText.selectionStart).isEqualTo(6)
    assertThat(editText.selectionEnd).isEqualTo(6)
  }

  @Test
  fun replacingSixInYear_changes1965To1975() {
    editText.setText("19/11/1965")

    editText.setSelection(8, 9)
    editText.text?.replace(8, 9, "7")

    assertThat(editText.text.toString())
        .isEqualTo("19/11/1975")
  }

  @Test
  fun replacingLastYearDigit_keepsCorrectDate() {
    editText.setText("19/11/1965")

    editText.setSelection(9, 10)
    editText.text?.replace(9, 10, "7")

    assertThat(editText.text.toString()).isEqualTo("19/11/1967")
  }

  @Test
  fun replacingMonth_keepsDateFormatting() {
    editText.setText("19/11/1965")

    editText.setSelection(3, 5)
    editText.text?.replace(3, 5, "12")

    assertThat(editText.text.toString()).isEqualTo("19/12/1965")
  }

  @Test
  fun replacingDay_keepsDateFormatting() {
    editText.setText("19/11/1965")

    editText.setSelection(0, 2)
    editText.text?.replace(0, 2, "20")

    assertThat(editText.text.toString()).isEqualTo("20/11/1965")
  }

  @Test
  fun deletingLastDigit_keepsRemainingDate() {
    editText.setText("19/11/1965")

    editText.text?.delete(9, 10)

    assertThat(editText.text.toString()).isEqualTo("19/11/196")
  }

  @Test
  fun deletingYearDigit_keepsDateFormatting() {
    editText.setText("19/11/1965")

    editText.text?.delete(7, 8)

    assertThat(editText.text.toString()).isEqualTo("19/11/165")
  }

  @Test
  fun deletingEntireDate_clearsField() {
    editText.setText("19/11/1965")

    editText.text?.delete(0, editText.text!!.length)

    assertThat(editText.text.toString()).isEmpty()
  }

  @Test
  fun replacingEntireYear_changesDateCorrectly() {
    editText.setText("19/11/1965")

    editText.setSelection(6, 10)
    editText.text?.replace(6, 10, "1975")

    assertThat(editText.text.toString())
        .isEqualTo("19/11/1975")
  }

  @Test
  fun replacingEntireDate_formatsNewDate() {
    editText.setText("19/11/1965")

    editText.setSelection(0, 10)
    editText.text?.replace(0, 10, "10041995")

    assertThat(editText.text.toString())
        .isEqualTo("10/04/1995")
  }

  @Test
  fun replacingFirstYearDigit() {
    editText.setText("19/11/1965")

    editText.text?.replace(6, 7, "2")

    assertThat(editText.text.toString())
        .isEqualTo("19/11/2965")
  }

  @Test
  fun replacingSecondYearDigit() {
    editText.setText("19/11/1965")

    editText.text?.replace(7, 8, "8")

    assertThat(editText.text.toString())
        .isEqualTo("19/11/1865")
  }

  @Test
  fun replacingThirdYearDigit() {
    editText.setText("19/11/1965")

    editText.text?.replace(8, 9, "7")

    assertThat(editText.text.toString())
        .isEqualTo("19/11/1975")
  }

  @Test
  fun replacingFourthYearDigit() {
    editText.setText("19/11/1965")

    editText.text?.replace(9, 10, "8")

    assertThat(editText.text.toString())
        .isEqualTo("19/11/1968")
  }

  @Test
  fun setTextWithWatcher_formatsExistingDate() {
    val watcher = object : android.text.TextWatcher {
      override fun beforeTextChanged(
          s: CharSequence?,
          start: Int,
          count: Int,
          after: Int
      ) = Unit

      override fun onTextChanged(
          s: CharSequence?,
          start: Int,
          before: Int,
          count: Int
      ) = Unit

      override fun afterTextChanged(s: android.text.Editable?) = Unit
    }

    editText.setTextWithWatcher(
        "19/11/1965",
        watcher
    )

    assertThat(editText.text.toString())
        .isEqualTo("19/11/1965")
  }
}
