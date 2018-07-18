package org.simple.clinic.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.setTextAndCursor
import javax.inject.Inject

class PatientSearchAgeFilterSheet : BottomSheetActivity() {

  @Inject
  lateinit var activity: TheActivity

  private val ageEditText by bindView<EditText>(R.id.agefilter_age)
  private val resetButton by bindView<Button>(R.id.agefilter_reset)
  private val applyButton by bindView<Button>(R.id.agefilter_apply)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_search_age_filter)
    TheActivity.component.inject(this)

    val ageText = intent.extras.getString(EXTRA_AGE)
    ageEditText.setTextAndCursor(ageText)

    ageEditText.setOnEditorActionListener { _, actionId, _ ->
      when (actionId) {
        EditorInfo.IME_ACTION_DONE -> {
          applyButton.performClick()
          true
        }
        else -> false
      }
    }

    applyButton.setOnClickListener {
      val intent = Intent()
      intent.putExtra(EXTRA_AGE, SearchQueryAgeChanged(ageEditText.text.toString()))
      setResult(Activity.RESULT_OK, intent)
      finish()
    }

    resetButton.setOnClickListener {
      val intent = Intent()
      intent.putExtra(EXTRA_AGE, SearchQueryAgeChanged(""))
      setResult(Activity.RESULT_OK, intent)
      finish()
    }
  }

  companion object {
    private const val EXTRA_AGE = "age"

    fun extractResult(data: Intent): SearchQueryAgeChanged {
      return data.getParcelableExtra(EXTRA_AGE)
    }

    fun intent(context: Context, ageText: String): Intent {
      return Intent(context, PatientSearchAgeFilterSheet::class.java)
        .putExtra(EXTRA_AGE, ageText)
    }
  }
}
