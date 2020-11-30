package org.simple.clinic.deeplink

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import org.simple.clinic.ClinicApp
import org.simple.clinic.deeplink.di.DeepLinkComponent
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.feature.Features
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.setup.SetupActivity
import org.simple.clinic.util.asUuid
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class DeepLinkActivity : AppCompatActivity(), DeepLinkUiActions {

  companion object {
    private const val PATIENT_UUID_QUERY_KEY = "p"
    private const val TELECONSULT_RECORD_ID_QUERY_KEY = "r"
  }

  @Inject
  lateinit var effectHandler: DeepLinkEffectHandler.Factory

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  private val deepLinkData by unsafeLazy {
    intent.data
  }

  private val isLogTeleconsultDeepLink by unsafeLazy {
    deepLinkData?.queryParameterNames.isNullOrEmpty().not()
  }

  private val delegate: MobiusDelegate<DeepLinkModel, DeepLinkEvent, DeepLinkEffect> by unsafeLazy {
    MobiusDelegate.forActivity(
        events = Observable.empty(),
        defaultModel = DeepLinkModel.default(
            patientUuid = patientUuid(),
            teleconsultRecordId = teleconsultRecordId(),
            isLogTeleconsultDeepLink = isLogTeleconsultDeepLink
        ),
        update = DeepLinkUpdate(),
        init = DeepLinkInit(),
        effectHandler = effectHandler.create(this).build()
    )
  }

  private fun patientUuid(): UUID? {
    val patientIdString = if (isLogTeleconsultDeepLink) {
      deepLinkData?.getQueryParameter(PATIENT_UUID_QUERY_KEY)
    } else {
      deepLinkData?.lastPathSegment
    }

    return patientIdString?.asUuid()
  }

  private fun teleconsultRecordId(): UUID? {
    val teleconsultRecordIdString = if (isLogTeleconsultDeepLink) {
      deepLinkData?.getQueryParameter(TELECONSULT_RECORD_ID_QUERY_KEY)
    } else {
      null
    }

    return teleconsultRecordIdString?.asUuid()
  }

  private lateinit var component: DeepLinkComponent

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
        .deepLinkComponent()
        .activity(this)
        .build()

    component.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    super.onStop()
    delegate.stop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
  }

  override fun navigateToSetupActivity() {
    val intent = Intent(this, SetupActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
    finish()
  }

  override fun navigateToPatientSummary(patientUuid: UUID) {
    val intent = TheActivity.intentForOpenPatientSummary(this, patientUuid)
    startActivity(intent)
    finish()
  }

  override fun navigateToPatientSummaryWithTeleconsultLog(patientUuid: UUID, teleconsultRecordId: UUID) {
    val intent = TheActivity.intentForOpenPatientSummaryWithTeleconsultLog(this, patientUuid, teleconsultRecordId)
    startActivity(intent)
    finish()
  }

  override fun showPatientDoesNotExist() {
    val intent = TheActivity.intentForShowPatientNotFoundError(this)
    startActivity(intent)
    finish()
  }

  override fun showNoPatientUuidError() {
    val intent = TheActivity.intentForShowNoPatientUuidError(this)
    startActivity(intent)
    finish()
  }

  override fun showTeleconsultLogNotAllowed() {
    val intent = TheActivity.intentForShowTeleconsultNotAllowedError(this)
    startActivity(intent)
    finish()
  }
}
