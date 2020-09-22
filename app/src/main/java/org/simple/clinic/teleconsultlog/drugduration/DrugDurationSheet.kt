package org.simple.clinic.teleconsultlog.drugduration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding3.widget.editorActions
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.sheet_drug_duration.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.teleconsultlog.drugduration.di.DrugDurationComponent
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.textChanges
import java.time.Duration
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class DrugDurationSheet : BottomSheetActivity(), DrugDurationUi, DrugDurationUiActions {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var drugDurationUpdate: DrugDurationUpdate

  @Inject
  lateinit var effectHandlerFactory: DrugDurationEffectHandler.Factory

  companion object {
    private const val EXTRA_DRUG_DURATION = "drugDuration"
    private const val EXTRA_SAVED_DRUG_UUID = "savedDrugUuid"
    private const val EXTRA_SAVED_DURATION = "savedDrugDuration"

    fun intent(
        context: Context,
        drugDuration: DrugDuration
    ): Intent {
      return Intent(context, DrugDurationSheet::class.java).apply {
        putExtra(EXTRA_DRUG_DURATION, drugDuration)
      }
    }

    fun readSavedDrugDuration(intent: Intent): SavedDrugDuration {
      val uuid = intent.getSerializableExtra(EXTRA_SAVED_DRUG_UUID) as UUID
      val duration = intent.extras!!.getInt(EXTRA_SAVED_DURATION)

      return SavedDrugDuration(
          drugUuid = uuid,
          duration = Duration.ofDays(duration.toLong())
      )
    }
  }

  private lateinit var component: DrugDurationComponent

  private val drugDuration by unsafeLazy {
    intent.getParcelableExtra<DrugDuration>(EXTRA_DRUG_DURATION)!!
  }

  private val events by unsafeLazy {
    Observable
        .merge(
            imeClicks(),
            durationChanges()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = DrugDurationUiRenderer(this)

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = DrugDurationModel.create(drugDuration.duration),
        init = DrugDurationInit(),
        update = drugDurationUpdate,
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_drug_duration)
    delegate.onRestoreInstanceState(savedInstanceState)

    drugDurationTitleTextView.text = getString(R.string.drug_duration_title, drugDuration.name, drugDuration.dosage)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDi()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  private fun setupDi() {
    component = ClinicApp.appComponent
        .drugDurationComponent()
        .activity(this)
        .build()

    component.inject(this)
  }

  private fun imeClicks(): Observable<DrugDurationEvent> {
    return drugDurationEditText
        .editorActions { it == EditorInfo.IME_ACTION_DONE }
        .map { DrugDurationSaveClicked }
  }

  private fun durationChanges(): Observable<DrugDurationEvent> {
    return drugDurationEditText
        .textChanges { DurationChanged(it) }
  }

  override fun showBlankDurationError() {
    drugDurationErrorTextView.text = getString(R.string.drug_duration_empty_error)
    drugDurationErrorTextView.visibility = View.VISIBLE
  }

  override fun showMaxDrugDurationError(maxAllowedDurationInDays: Int) {
    drugDurationErrorTextView.text = getString(R.string.drug_duration_max_error, maxAllowedDurationInDays.toString())
    drugDurationErrorTextView.visibility = View.VISIBLE
  }

  override fun hideDurationError() {
    drugDurationErrorTextView.text = null
    drugDurationErrorTextView.visibility = View.GONE
  }

  override fun saveDrugDuration(duration: Int) {
    val intent = Intent().apply {
      putExtra(EXTRA_SAVED_DRUG_UUID, drugDuration.uuid)
      putExtra(EXTRA_SAVED_DURATION, duration)
    }
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  override fun prefillDrugDuration(duration: String) {
    drugDurationEditText.setTextAndCursor(duration)
  }
}
