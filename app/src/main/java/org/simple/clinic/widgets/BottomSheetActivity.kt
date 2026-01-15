package org.simple.clinic.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import org.simple.clinic.databinding.BottomSheetBinding
import org.simple.clinic.util.disablePendingTransitions
import org.simple.clinic.util.handleBackPress

/**
 * We're using Activities as fake bottom sheets instead of BottomSheetDialog because we want
 * our layout to be entirely anchored on top of the keyboard. With BottomSheetDialog, the
 * keyboard otherwise aligns itself just below text fields, overlapping everything else
 * present below them.
 *
 * TODO: Add BottomSheet behavior to dismiss the sheet by dragging it downwards.
 */
abstract class BottomSheetActivity : AppCompatActivity() {

  private lateinit var bottomSheetBinding: BottomSheetBinding

  private val backgroundView
    get() = bottomSheetBinding.bottomsheetBackground

  private val contentContainer
    get() = bottomSheetBinding.bottomsheetContentContainer

  override fun onCreate(savedInstanceState: Bundle?) {
    disablePendingTransitions()
    super.onCreate(savedInstanceState)
    bottomSheetBinding = BottomSheetBinding.inflate(layoutInflater)
    super.setContentView(bottomSheetBinding.root)

    handleBackPress {
      finish()
    }

    contentContainer.setOnClickListener {
      // Swallow clicks to avoid dismissing the sheet accidentally.
    }

    backgroundView.setOnClickListener {
      onBackgroundClick()
    }

    animateBottomSheetIn(
        backgroundView = backgroundView,
        contentContainer = contentContainer
    )
  }

  override fun finish() {
    animateBottomSheetOut(
        backgroundView = backgroundView,
        contentContainer = contentContainer,
        endAction = {
          super.finish()
          disablePendingTransitions()
        }
    )
  }

  override fun setContentView(layoutResId: Int) {
    LayoutInflater.from(this).inflate(layoutResId, contentContainer)
  }

  override fun setContentView(view: View?) {
    contentContainer.addView(view)
  }

  override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
    contentContainer.addView(view, params)
  }

  open fun onBackgroundClick() {
    onBackPressedDispatcher.onBackPressed()
  }
}
