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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_teleconsult_share_prescription.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named


class TeleconsultSharePrescriptionScreen constructor(
    context: Context,
    attributeSet: AttributeSet?
) : ConstraintLayout(context, attributeSet), TeleconsultSharePrescriptionUi, TeleconsultSharePrescriptionUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var effectHandler: TeleconsultSharePrescriptionEffectHandler.Factory

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  private val screenKey by unsafeLazy {
    screenRouter.key<TeleconsultSharePrescriptionScreenKey>(this)
  }

  private val events: Observable<UiEvent> by unsafeLazy {
    Observable
        .merge(
            downloadClicks(),
            shareClicks(),
            doneClicks()
        )
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

  private val teleconsultSharePrescriptionMedicinesAdapter = ItemAdapter(TeleconsultSharePrescriptionDiffCallback())

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return
    context.injector<Injector>().inject(this)

    showMedicalInstructions()
    medicinesRecyclerView.adapter = teleconsultSharePrescriptionMedicinesAdapter
  }

  private fun showMedicalInstructions() {
    instructionsForNextVisitTextView.text = screenKey.medicalInstructions
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

  private fun sharePrescription(imageUri: Uri) {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "image/png"
    sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
    context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.screen_teleconsult_share_prescription_image_using)))
  }

  override fun renderPrescriptionDate(prescriptionDate: LocalDate) {
    prescriptionDateTextView.text = dateFormatter.format(prescriptionDate)
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
        TeleconsultSharePrescriptionItem.from(medicines = medicines))
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

