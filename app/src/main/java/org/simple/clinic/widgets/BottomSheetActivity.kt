package org.simple.clinic.widgets

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

// TODO: Add background dimming
// TODO: Dismiss on background tap
// TODO: Entry and exit animations.
abstract class BottomSheetActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    overridePendingTransition(0, 0)
    super.onCreate(savedInstanceState)
  }

  override fun finish() {
    super.finish()
    overridePendingTransition(0, 0)
  }
}
