package org.simple.clinic.summary.teleconsultation.contactdoctor

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListContactDoctorBinding
import org.simple.clinic.databinding.SheetContactDoctorBinding
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.summary.PatientTeleconsultationInfo
import org.simple.clinic.summary.teleconsultation.messagebuilder.LongTeleconsultMessageBuilder
import org.simple.clinic.summary.teleconsultation.messagebuilder.ShortTeleconsultMessageBuilder
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import org.simple.clinic.util.messagesender.SmsMessageSender
import org.simple.clinic.util.messagesender.WhatsAppMessageSender
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.dp
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ContactDoctorSheet : BottomSheetActivity(), ContactDoctorUi, ContactDoctorUiActions {

  private lateinit var binding: SheetContactDoctorBinding

  private val doctorsRecyclerView
    get() = binding.doctorsRecyclerView

  companion object {
    private const val EXTRA_PATIENT_UUID = "patientUuid"

    fun intent(context: Context, patientUuid: UUID): Intent {
      return Intent(context, ContactDoctorSheet::class.java).apply {
        putExtra(EXTRA_PATIENT_UUID, patientUuid)
      }
    }
  }

  @Inject
  lateinit var effectHandlerFactory: ContactDoctorEffectHandler.Factory

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var longTeleconsultMessageBuilder: LongTeleconsultMessageBuilder

  @Inject
  lateinit var shortTeleconsultMessageBuilder: ShortTeleconsultMessageBuilder

  @Inject
  lateinit var whatsAppMessageSender: WhatsAppMessageSender

  @Inject
  lateinit var smsMessageSender: SmsMessageSender

  @Inject
  lateinit var features: Features

  private val itemAdapter = ItemAdapter(
      diffCallback = DoctorListItem.DiffCallback(),
      bindings = mapOf(
          R.layout.list_contact_doctor to { layoutInflater, parent ->
            ListContactDoctorBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val events by unsafeLazy {
    Observable
        .merge(
            whatsAppButtonClicks(),
            smsButtonClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = ContactDoctorUiRenderer(this)
    val patientUuid = intent.getSerializableExtra(EXTRA_PATIENT_UUID) as UUID

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = ContactDoctorModel.create(patientUuid),
        init = ContactDoctorInit(),
        update = ContactDoctorUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private lateinit var component: ContactDoctorComponent

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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = SheetContactDoctorBinding.inflate(layoutInflater)
    setContentView(binding.root)
    delegate.onRestoreInstanceState(savedInstanceState)

    doctorsRecyclerView.adapter = itemAdapter
    doctorsRecyclerView.addItemDecoration(DividerItemDecorator(
        context = this,
        marginStart = 16.dp,
        marginEnd = 16.dp
    ))
  }

  override fun showMedicalOfficers(medicalOfficers: List<MedicalOfficer>) {
    itemAdapter.submitList(DoctorListItem.from(medicalOfficers))
  }

  override fun sendTeleconsultMessage(teleconsultInfo: PatientTeleconsultationInfo, messageTarget: MessageTarget) {
    when (messageTarget) {
      MessageTarget.WHATSAPP -> sendWhatsAppMessage(teleconsultInfo)
      MessageTarget.SMS -> sendSmsMessage(teleconsultInfo)
    }
  }

  private fun sendWhatsAppMessage(teleconsultInfo: PatientTeleconsultationInfo) {
    val message = longTeleconsultMessageBuilder.message(teleconsultInfo)
    whatsAppMessageSender.send(teleconsultInfo.doctorPhoneNumber!!, message)
  }

  private fun sendSmsMessage(teleconsultInfo: PatientTeleconsultationInfo) {
    val message = shortTeleconsultMessageBuilder.message(teleconsultInfo)
    smsMessageSender.send(teleconsultInfo.doctorPhoneNumber!!, message)
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(locale, features))
  }

  private fun whatsAppButtonClicks(): Observable<UiEvent> {
    return itemAdapter
        .itemEvents
        .ofType<DoctorListItem.Event.WhatsAppClicked>()
        .map { WhatsAppButtonClicked(it.doctorId, it.phoneNumber) }
  }

  private fun smsButtonClicks(): Observable<UiEvent> {
    return itemAdapter
        .itemEvents
        .ofType<DoctorListItem.Event.SmsClicked>()
        .map { SmsButtonClicked(it.doctorId, it.phoneNumber) }
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .contactDoctorComponent()
        .create(activity = this)

    component.inject(this)
  }
}
