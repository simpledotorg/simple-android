package org.simple.clinic.summary.teleconsultation.status

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View.NO_ID
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetTeleconsultStatusBinding
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus.No
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus.StillWaiting
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus.Yes
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class TeleconsultStatusSheet : BottomSheetActivity(), TeleconsultStatusUi, TeleconsultStatusUiAction {

  private lateinit var binding: SheetTeleconsultStatusBinding

  private val teleconsultStatusDoneButton
    get() = binding.teleconsultStatusDoneButton

  private val teleconsultStatusRadioGroup
    get() = binding.teleconsultStatusRadioGroup

  companion object {
    private const val EXTRA_TELECONSULT_RECORD_ID = "teleconsultRecordId"

    fun intent(
        context: Context,
        teleconsultRecordId: UUID
    ): Intent {
      return Intent(context, TeleconsultStatusSheet::class.java).apply {
        putExtra(EXTRA_TELECONSULT_RECORD_ID, teleconsultRecordId)
      }
    }
  }

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var effectHandlerFactory: TeleconsultStatusEffectHandler.Factory

  @Inject
  lateinit var features: Features

  private lateinit var component: TeleconsultStatusComponent

  private val teleconsultRecordId by unsafeLazy {
    intent.getSerializableExtra(EXTRA_TELECONSULT_RECORD_ID) as UUID
  }

  private val teleconsultRadioButtonIdsToTeleconsultStatus = mapOf(
      R.id.teleconsultStatusYesRadioButton to Yes,
      R.id.teleconsultStatusNoRadioButton to No,
      R.id.teleconsultStatusWaitingRadioButton to StillWaiting
  )

  private val events by unsafeLazy {
    Observable
        .merge(
            doneClicks(),
            teleconsultChanges()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = TeleconsultStatusUiRenderer(this)

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = TeleconsultStatusModel.create(
            teleconsultRecordId = teleconsultRecordId
        ),
        update = TeleconsultStatusUpdate(),
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
    binding = SheetTeleconsultStatusBinding.inflate(layoutInflater)
    setContentView(binding.root)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDi()

    val wrappedContext = baseContext
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(locale, features))
  }

  private fun setupDi() {
    component = ClinicApp.appComponent
        .teleconsultStatusComponent()
        .activity(this)
        .build()

    component.inject(this)
  }

  override fun enableDoneButton() {
    teleconsultStatusDoneButton.isEnabled = true
  }

  override fun dismissSheet() {
    val intent = Intent()
    setResult(Activity.RESULT_OK, intent)

    finish()
  }

  private fun doneClicks(): Observable<UiEvent> {
    return teleconsultStatusDoneButton
        .clicks()
        .map { DoneClicked }
  }

  private fun teleconsultChanges(): Observable<UiEvent> {
    return teleconsultStatusRadioGroup
        .checkedChanges()
        .filter { it != NO_ID }
        .map { id -> TeleconsultStatusChanged(teleconsultRadioButtonIdsToTeleconsultStatus.getValue(id)) }
  }
}
