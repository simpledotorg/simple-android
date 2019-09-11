package experiments

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.f2prateek.rx.preferences2.Preference
import kotlinx.android.synthetic.main.experiments_activitytoggle.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import javax.inject.Inject
import javax.inject.Named

class ExperimentsToggleActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.experiments_activitytoggle)
    setSupportActionBar(toolBar)
    if (savedInstanceState == null) {
      supportFragmentManager
          .beginTransaction()
          .add(R.id.container, ExperimentsToggleFragment())
          .commit()
    }
  }

  class ExperimentsToggleFragment : PreferenceFragmentCompat() {

    @field:[Inject Named("experiment_instantsearch_v1_toggle")]
    lateinit var instantSearchV1ExperimentToggle: Preference<Boolean>

    override fun onAttach(context: Context) {
      super.onAttach(context)
      TheActivity.component.experimentsComponentBuilder().build().inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      val context = preferenceManager.context
      val screen = preferenceManager.createPreferenceScreen(context)

      val toggleInstantSearchV1Experiment = SwitchPreferenceCompat(context).apply {
        key = instantSearchV1ExperimentToggle.key()
        title = "Instant Search (v1)"
        summary = "Toggle the instant search experiment (v1) flow"
      }

      screen.addPreference(toggleInstantSearchV1Experiment)

      preferenceScreen = screen
    }
  }
}
