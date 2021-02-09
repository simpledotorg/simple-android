package org.simple.clinic.teleconsultlog.shareprescription

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListItemTeleconsultSharePrescriptionMedicineBinding
import org.simple.clinic.databinding.ScreenTeleconsultSharePrescriptionBinding
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.teleconsultlog.prescription.medicines.TeleconsultMedicinesConfig
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class TeleconsultSharePrescriptionScreen :
    BaseScreen<
        TeleconsultSharePrescriptionScreenKey,
        ScreenTeleconsultSharePrescriptionBinding,
        TeleconsultSharePrescriptionModel,
        TeleconsultSharePrescriptionEvent,
        TeleconsultSharePrescriptionEffect>(),
    TeleconsultSharePrescriptionUi, TeleconsultSharePrescriptionUiActions {

  private val medicinesRecyclerView
    get() = binding.medicinesRecyclerView

  private val instructionsTextView
    get() = binding.instructionsTextView

  private val shareButton
    get() = binding.shareButton

  private val layoutSharePrescription
    get() = binding.layoutSharePrescription

  private val downloadButton
    get() = binding.downloadButton

  private val doneButton
    get() = binding.doneButton

  private val toolbar
    get() = binding.toolbar

  private val signatureImageView
    get() = binding.signatureImageView

  private val prescriptionDateTextView
    get() = binding.prescriptionDateTextView

  private val medicalRegistrationIdTextView
    get() = binding.medicalRegistrationIdTextView

  private val patientAddressTextView
    get() = binding.patientAddressTextView

  private val patientNameTextView
    get() = binding.patientNameTextView

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var effectHandler: TeleconsultSharePrescriptionEffectHandler.Factory

  @Inject
  lateinit var teleconsultMedicinesConfig: TeleconsultMedicinesConfig

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  private val imageSavedMessageEvents = PublishSubject.create<UiEvent>()

  private val teleconsultSharePrescriptionMedicinesAdapter = ItemAdapter(
      diffCallback = TeleconsultSharePrescriptionDiffCallback(),
      bindings = mapOf(
          R.layout.list_item_teleconsult_share_prescription_medicine to { layoutInflater, parent ->
            ListItemTeleconsultSharePrescriptionMedicineBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  override fun defaultModel(): TeleconsultSharePrescriptionModel {
    return TeleconsultSharePrescriptionModel.create(screenKey.patientUuid, LocalDate.now(userClock))
  }

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?): ScreenTeleconsultSharePrescriptionBinding {
    return ScreenTeleconsultSharePrescriptionBinding.inflate(layoutInflater, container, false)
  }

  override fun uiRenderer(): ViewRenderer<TeleconsultSharePrescriptionModel> {
    return TeleconsultSharePrescriptionUiRenderer(this)
  }

  override fun events(): Observable<TeleconsultSharePrescriptionEvent> {
    return Observable
        .mergeArray(
            downloadClicks(),
            shareClicks(),
            doneClicks(),
            backClicks(),
            imageSavedMessageEvents
        )
        .compose(RequestPermissions(runtimePermissions, screenResults.streamResults().ofType()))
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun createUpdate() = TeleconsultSharePrescriptionUpdate()

  override fun createInit() = TeleconsultSharePrescriptionInit()

  override fun createEffectHandler() = effectHandler.create(this).build()

  override fun onAttach(context: Context) {
    context.injector<Injector>().inject(this)
    super.onAttach(context)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    showMedicalInstructions()
    medicinesRecyclerView.adapter = teleconsultSharePrescriptionMedicinesAdapter
  }

  private fun showMedicalInstructions() {
    val medicalInstructions = screenKey.medicalInstructions

    if (medicalInstructions.isNullOrBlank()) {
      instructionsTextView.text = requireContext().getString(R.string.screen_teleconsult_share_prescription_instructions_empty)
    } else {
      instructionsTextView.text = medicalInstructions
    }
  }

  private fun shareClicks(): Observable<UiEvent> = shareButton
      .clicks()
      .map {
        val bitmap = getScaledBitmap(layoutSharePrescription.width, layoutSharePrescription.height, layoutSharePrescription)
        ShareClicked(bitmap)
      }

  private fun downloadClicks() = downloadButton
      .clicks()
      .map {
        val bitmap = getScaledBitmap(layoutSharePrescription.width, layoutSharePrescription.height, layoutSharePrescription)
        DownloadClicked(bitmap)
      }

  private fun doneClicks() = doneButton
      .clicks()
      .map { DoneClicked }

  private fun backClicks() = toolbar
      .navigationClicks()
      .map { BackClicked }

  override fun setSignatureBitmap(bitmap: Bitmap) {
    signatureImageView.setImageBitmap(bitmap)
  }

  override fun setMedicalRegistrationId(medicalRegistrationId: String) {
    medicalRegistrationIdTextView.text = requireContext().getString(
        R.string.screen_teleconsult_share_prescription_medical_registration_id,
        medicalRegistrationId
    )
  }

  override fun openHomeScreen() {
    router.clearHistoryAndPush(HomeScreenKey)
  }

  override fun sharePrescriptionAsImage(imageUri: Uri) {
    sharePrescription(imageUri)
  }

  override fun goToPreviousScreen() {
    router.pop()
  }

  private fun sharePrescription(imageUri: Uri) {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "image/png"
    sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    requireContext().startActivity(Intent.createChooser(
        sharingIntent,
        requireContext().getString(R.string.screen_teleconsult_share_prescription_image_using)
    ))
  }

  override fun renderPrescriptionDate(prescriptionDate: LocalDate) {
    // Since we need the date shown in prescription image to be english, we are using english locale here
    prescriptionDateTextView.text = dateFormatter.withLocale(Locale.ENGLISH).format(prescriptionDate)
  }

  override fun renderPatientInformation(patientProfile: PatientProfile) {
    patientAddressTextView.text = patientProfile.address.completeAddress
    val ageValue = DateOfBirth.fromPatient(patientProfile.patient, userClock).estimateAge(userClock)
    val patientGender = patientProfile.patient.gender
    patientNameTextView.text = requireContext().getString(
        R.string.screen_teleconsult_share_prescription_patient_details,
        patientProfile.patient.fullName,
        requireContext().getString(patientGender.displayLetterRes),
        ageValue.toString()
    )
  }

  override fun renderPatientMedicines(medicines: List<PrescribedDrug>) {
    teleconsultSharePrescriptionMedicinesAdapter.submitList(
        TeleconsultSharePrescriptionItem.from(
            medicines = medicines,
            defaultDuration = teleconsultMedicinesConfig.defaultDuration,
            defaultFrequency = teleconsultMedicinesConfig.defaultFrequency
        ))
  }

  override fun showImageSavedToast() {
    val message = requireContext().getString(R.string.screen_teleconsult_share_prescription_image_saved)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    imageSavedMessageEvents.onNext(ImageSavedMessageShown)
  }

  override fun showDownloadProgress() {
    downloadButton.setButtonState(InProgress)
  }

  override fun hideDownloadProgress() {
    downloadButton.setButtonState(Enabled)
  }

  override fun showShareProgress() {
    shareButton.setButtonState(InProgress)
  }

  override fun hideShareProgress() {
    shareButton.setButtonState(Enabled)
  }

  private fun getScaledBitmap(width: Int, height: Int, view: View): Bitmap {
    val targetWidth = 1500f
    val sourceWidth = width.toFloat()
    val scaleFactor = targetWidth / sourceWidth
    val sourceHeight = height.toFloat()

    val bitmapWidth = (sourceWidth * scaleFactor).toInt()
    val bitmapHeight = (sourceHeight * scaleFactor).toInt()
    val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val matrix = Matrix().apply {
      postScale(scaleFactor, scaleFactor)
    }

    canvas.concat(matrix)
    view.draw(canvas)
    return bitmap
  }

  interface Injector {
    fun inject(target: TeleconsultSharePrescriptionScreen)
  }
}
