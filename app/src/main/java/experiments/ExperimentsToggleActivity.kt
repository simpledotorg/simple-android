package experiments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.experiments_activitytoggle.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

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

    @Inject
    lateinit var patientRepository: PatientRepository

    @Inject
    lateinit var bloodPressureRepository: BloodPressureRepository

    @Inject
    lateinit var prescriptionRepository: PrescriptionRepository

    override fun onAttach(context: Context) {
      super.onAttach(context)
      TheActivity.component.experimentsComponentBuilder().build().inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      val context = preferenceManager.context
      val screen = preferenceManager.createPreferenceScreen(context)

      val toggleInstantSearchV1Experiment = SwitchPreferenceCompat(context).apply {
        key = "experiment_instantsearch_v1_toggle"
        title = "Instant Search (v1)"
        summary = "Toggle the instant search experiment (v1) flow"
      }

      val resetSeedData = Preference(context).apply {
        key = "reset_seed_data"
        title = "Reset Seed Data"
        isPersistent = false
        summary = "Clears the patient data already saved and resets the data to the default seed data"
        setOnPreferenceClickListener {
          resetSeedData()
          true
        }
      }

      screen.addPreference(toggleInstantSearchV1Experiment)
      screen.addPreference(resetSeedData)

      preferenceScreen = screen
    }

    @SuppressLint("CheckResult")
    private fun resetSeedData() {
      val dialog = AlertDialog.Builder(requireContext())
          .setTitle("Resetting Seed Data")
          .setMessage("Please wait\u2026")
          .setCancelable(false)
          .create()

      val saveSeedData = Observable.fromIterable(ExperimentData.seedData)
          .flatMapCompletable { seedDataRecord ->
            val phoneNumbers = if (seedDataRecord.phoneNumber != null) listOf(seedDataRecord.phoneNumber) else emptyList()

            val patientProfile = PatientProfile(
                seedDataRecord.patient,
                seedDataRecord.address,
                phoneNumbers,
                emptyList()
            )

            patientRepository.save(listOf(patientProfile))
                .andThen(bloodPressureRepository.save(seedDataRecord.bloodPressureMeasurements))
                .andThen(prescriptionRepository.save(seedDataRecord.prescribedDrugs))
          }


      patientRepository
          .clearPatientData()
          .andThen(saveSeedData)
          .doOnSubscribe { requireActivity().runOnUiThread { dialog.show() } }
          .subscribeOn(io())
          .delay(3L, SECONDS)
          .observeOn(mainThread())
          .subscribe { dialog.dismiss() }
    }
  }
}
