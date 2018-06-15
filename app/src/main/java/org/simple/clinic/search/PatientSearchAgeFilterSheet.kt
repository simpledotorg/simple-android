package org.simple.clinic.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.TheActivity
import javax.inject.Inject

class PatientSearchAgeFilterSheet : AppCompatActivity() {

  @Inject
  lateinit var activity: TheActivity

  private val backgroundContainer by bindView<View>(R.id.agefilter_root)
  private val ageEditText by bindView<EditText>(R.id.agefilter_age)
  private val resetButton by bindView<Button>(R.id.agefilter_reset)
  private val applyButton by bindView<Button>(R.id.agefilter_apply)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_search_age_filter)
    TheActivity.component.inject(this)

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

    backgroundContainer.setOnClickListener {
      finish()
    }
  }

  companion object {
    private const val EXTRA_AGE = "age"

    fun extract(data: Intent): SearchQueryAgeChanged {
      return data.getParcelableExtra(EXTRA_AGE)
    }

    fun intent(context: Context): Intent {
      return Intent(context, PatientSearchAgeFilterSheet::class.java)
    }
  }
}
