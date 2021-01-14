package org.simple.clinic.teleconsultlog.shareprescription

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListItemTeleconsultSharePrescriptionMedicineBinding
import org.simple.clinic.databinding.ScreenTeleconsultSharePrescriptionBinding
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.teleconsultlog.prescription.medicines.TeleconsultMedicinesConfig
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named


class TeleconsultSharePrescriptionScreen constructor(
    context: Context,
    attributeSet: AttributeSet?
) : ConstraintLayout(context, attributeSet), TeleconsultSharePrescriptionUi, TeleconsultSharePrescriptionUiActions {

  private var binding: ScreenTeleconsultSharePrescriptionBinding? = null

  private val medicinesRecyclerView
    get() = binding!!.medicinesRecyclerView

  private val instructionsTextView
    get() = binding!!.instructionsTextView

  private val shareButton
    get() = binding!!.shareButton

  private val layoutSharePrescription
    get() = binding!!.layoutSharePrescription

  private val downloadButton
    get() = binding!!.downloadButton

  private val doneButton
    get() = binding!!.doneButton

  private val toolbar
    get() = binding!!.toolbar

  private val signatureImageView
    get() = binding!!.signatureImageView

  private val prescriptionDateTextView
    get() = binding!!.prescriptionDateTextView

  private val medicalRegistrationIdTextView
    get() = binding!!.medicalRegistrationIdTextView

  private val patientAddressTextView
    get() = binding!!.patientAddressTextView

  private val patientNameTextView
    get() = binding!!.patientNameTextView

  @Inject
  lateinit var screenRouter: ScreenRouter

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

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private val screenKey by unsafeLazy {
    screenKeyProvider.keyFor<TeleconsultSharePrescriptionScreenKey>(this)
  }

  private val imageSavedMessageEvents = PublishSubject.create<UiEvent>()
  private val events: Observable<UiEvent> by unsafeLazy {
    Observable
        .mergeArray(
            downloadClicks(),
            shareClicks(),
            doneClicks(),
            backClicks(),
            imageSavedMessageEvents
        )
        .compose(RequestPermissions(runtimePermissions, screenRouter.streamScreenResults().ofType()))
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = TeleconsultSharePrescriptionUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = TeleconsultSharePrescriptionModel.create(screenKey.patientUuid, LocalDate.now(userClock)),
        init = TeleconsultSharePrescriptionInit(),
        update = TeleconsultSharePrescriptionUpdate(),
        effectHandler = effectHandler.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private val teleconsultSharePrescriptionMedicinesAdapter = ItemAdapter(
      diffCallback = TeleconsultSharePrescriptionDiffCallback(),
      bindings = mapOf(
          R.layout.list_item_teleconsult_share_prescription_medicine to { layoutInflater, parent ->
            ListItemTeleconsultSharePrescriptionMedicineBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return
    context.injector<Injector>().inject(this)

    binding = ScreenTeleconsultSharePrescriptionBinding.bind(this)

    showMedicalInstructions()
    medicinesRecyclerView.adapter = teleconsultSharePrescriptionMedicinesAdapter
  }

  private fun showMedicalInstructions() {
    val medicalInstructions = screenKey.medicalInstructions

    if (medicalInstructions.isNullOrBlank()) {
      instructionsTextView.text = context.getString(R.string.screen_teleconsult_share_prescription_instructions_empty)
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
    medicalRegistrationIdTextView.text = context.getString(
        R.string.screen_teleconsult_share_prescription_medical_registration_id,
        medicalRegistrationId
    )
  }

  override fun openHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey, RouterDirection.REPLACE)
  }

  override fun sharePrescriptionAsImage(imageUri: Uri) {
    sharePrescription(imageUri)
  }

  override fun goToPreviousScreen() {
    screenRouter.pop()
  }

  private fun sharePrescription(imageUri: Uri) {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "image/png"
    sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.screen_teleconsult_share_prescription_image_using)))
  }

  override fun renderPrescriptionDate(prescriptionDate: LocalDate) {
    // Since we need the date shown in prescription image to be english, we are using english locale here
    prescriptionDateTextView.text = dateFormatter.withLocale(Locale.ENGLISH).format(prescriptionDate)
  }

  override fun renderPatientInformation(patientProfile: PatientProfile) {
    patientAddressTextView.text = patientProfile.address.completeAddress
    val ageValue = DateOfBirth.fromPatient(patientProfile.patient, userClock).estimateAge(userClock)
    val patientGender = patientProfile.patient.gender
    patientNameTextView.text = context.getString(
        R.string.screen_teleconsult_share_prescription_patient_details,
        patientProfile.patient.fullName,
        context.getString(patientGender.displayLetterRes),
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
    val message = context.getString(R.string.screen_teleconsult_share_prescription_image_saved)
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

