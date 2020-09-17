package org.simple.clinic.teleconsultlog.medicinefrequency

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.sheet_medicine_frequency.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.BD
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.OD
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.QDS
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency.TDS
import org.simple.clinic.teleconsultlog.medicinefrequency.di.MedicineFrequencyComponent
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class MedicineFrequencySheet : BottomSheetActivity(), MedicineFrequencySheetUiActions {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var effectHandlerFactory: MedicineFrequencyEffectHandler.Factory

  private lateinit var component: MedicineFrequencyComponent

  private val radioIdToMedicineFrequency = mapOf(
      R.id.medicineFrequencyOdRadioButton to OD,
      R.id.medicineFrequencyBdRadioButton to BD,
      R.id.medicineFrequencyTdsRadioButton to TDS,
      R.id.medicineFrequencyQdsRadioButton to QDS
  )

  companion object {
    private const val MEDICINE_FREQUENCY = "medicineFrequency"
    private const val EXTRA_SAVED_MEDICINE_UUID = "savedMedicineUuid"
    private const val EXTRA_SAVED_MEDICINE_FREQUENCY = "savedMedicineFrequency"

    fun intent(
        context: Context,
        medicineFrequencySheetExtra: MedicineFrequencySheetExtra
    ): Intent {
      return Intent(context, MedicineFrequencySheet::class.java).apply {
        putExtra(MEDICINE_FREQUENCY, medicineFrequencySheetExtra)
      }
    }

    fun readUuidAndFrequency(intent: Intent): Pair<UUID, MedicineFrequency> {
      val uuid = intent.getSerializableExtra(EXTRA_SAVED_MEDICINE_UUID) as UUID
      val medicineFrequency = intent.getParcelableExtra<MedicineFrequency>(EXTRA_SAVED_MEDICINE_FREQUENCY)!!

      return uuid to medicineFrequency
    }
  }

  private val medicineFrequencyExtra by unsafeLazy {
    intent.getParcelableExtra<MedicineFrequencySheetExtra>(MEDICINE_FREQUENCY)!!
  }

  private val events by unsafeLazy {
    Observable
        .merge(
            saveClicks(),
            medicineFrequencyChanges()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = MedicineFrequencyModel.create(medicineFrequency = medicineFrequencyExtra.medicineFrequency),
        init = MedicineFrequencyInit(),
        update = MedicineFrequencyUpdate(),
        effectHandler = effectHandlerFactory.create(this).build()
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_medicine_frequency)
    medicineFrequencyTitleTextView.text = getString(R.string.drug_duration_title, medicineFrequencyExtra.name, medicineFrequencyExtra.dosage)
  }

  private fun medicineFrequencyChanges(): Observable<MedicineFrequencyChanged> {
    return medicineFrequencyRadioGroup
        .checkedChanges()
        .filter { it != -1 }
        .map { checkedId ->
          val medicineFrequency = radioIdToMedicineFrequency.getValue(checkedId)
          MedicineFrequencyChanged(medicineFrequency)
        }
  }

  private fun saveClicks(): Observable<MedicineFrequencyEvent> {
    return saveMedicineFrequencyButton
        .clicks()
        .map { SaveMedicineFrequencyClicked }
  }

  override fun setMedicineFrequency(medicineFrequency: MedicineFrequency) {
    val id = when (medicineFrequency) {
      OD -> R.id.medicineFrequencyOdRadioButton
      BD -> R.id.medicineFrequencyBdRadioButton
      TDS -> R.id.medicineFrequencyTdsRadioButton
      QDS -> R.id.medicineFrequencyQdsRadioButton
      is MedicineFrequency.Unknown -> -1 // Don't check any radio button
    }
    medicineFrequencyRadioGroup.check(id)
  }

  override fun saveMedicineFrequency(medicineFrequency: MedicineFrequency) {
    val intent = Intent().apply {
      putExtra(EXTRA_SAVED_MEDICINE_UUID, medicineFrequencyExtra.uuid)
      putExtra(EXTRA_SAVED_MEDICINE_FREQUENCY, medicineFrequency)
    }
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun attachBaseContext(newBaseContext: Context) {
    setUpDependencyInjection()

    val context = newBaseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }
    super.attachBaseContext(context)

  }

  private fun setUpDependencyInjection() {
    component = ClinicApp.appComponent
        .medicineFrequencyComponent()
        .activity(this)
        .build()

    component.inject(this)
  }
}
