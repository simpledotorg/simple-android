package org.simple.clinic.widgets

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotterknife.bindView
import org.simple.clinic.R

/**
 * We're using Activities as fake bottom sheets instead of BottomSheetDialog because we want
 * our layout to be entirely anchored on top of the keyboard. With BottomSheetDialog, the
 * keyboard otherwise aligns itself just below text fields, overlapping everything else
 * present below them.
 *
 * TODO: Add background dimming
 * TODO: Dismiss on background tap
 * TODO: Entry and exit animations.
 */
abstract class BottomSheetActivity : AppCompatActivity() {

  private val rootLayout by bindView<ViewGroup>(R.id.bottomsheet_root)
  private val contentContainer by bindView<ViewGroup>(R.id.bottomsheet_content_container)

  override fun onCreate(savedInstanceState: Bundle?) {
    overridePendingTransition(0, 0)
    super.onCreate(savedInstanceState)
    super.setContentView(R.layout.bottom_sheet)

    rootLayout.setOnClickListener {
      onBackgroundClick()
    }
  }

  override fun finish() {
    super.finish()
    overridePendingTransition(0, 0)
  }

  override fun setContentView(layoutResId: Int) {
    LayoutInflater.from(this).inflate(layoutResId, contentContainer)
  }

  override fun setContentView(view: View) {
    contentContainer.addView(view)
  }

  override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
    contentContainer.addView(view, params)
  }

  open fun onBackgroundClick() {
    onBackPressed()
  }
}
