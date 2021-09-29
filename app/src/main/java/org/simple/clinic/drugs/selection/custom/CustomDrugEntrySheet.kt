package org.simple.clinic.drugs.selection.custom

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.editorActions
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetCustomDrugEntryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.PrescribedDrugsScreenKey
import org.simple.clinic.drugs.selection.custom.drugfrequency.SelectDrugFrequencyDialog
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.util.setFragmentResultListener
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import org.simple.clinic.widgets.textChanges
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class CustomDrugEntrySheet : BaseBottomSheet<
    CustomDrugEntrySheet.Key,
    SheetCustomDrugEntryBinding,
    CustomDrugEntryModel,
    CustomDrugEntryEvent,
    CustomDrugEntryEffect,
    Unit>(), CustomDrugEntryUi, CustomDrugEntrySheetUiActions {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: CustomDrugEntryEffectHandler.Factory

  private val hotEvents: PublishSubject<CustomDrugEntryEvent> = PublishSubject.create()

  private val openAs by unsafeLazy { screenKey.openAs }

  private val titleTextView
    get() = binding.titleTextView

  private val removeButton
    get() = binding.removeButton

  private val drugDosageEditText
    get() = binding.drugDosageEditText

  private val drugFrequencyEditText
    get() = binding.drugFrequencyEditText

  private val saveButton
    get() = binding.saveButton

  private val customDrugEntrySheetUiGroup
    get() = binding.customDrugEntrySheetUiGroup

  private val saveButtonFrame
    get() = binding.saveButtonFrame
    
  private val progressIndicator
    get() = binding.progressIndicator

  override fun defaultModel() = CustomDrugEntryModel.default(openAs, getString(R.string.custom_drug_entry_sheet_dosage_placeholder))

  override fun bindView(
      inflater: LayoutInflater,
      container: ViewGroup?
  ) = SheetCustomDrugEntryBinding.inflate(inflater, container, false)

  override fun uiRenderer() = CustomDrugEntryUiRenderer(this, getString(R.string.custom_drug_entry_sheet_dosage_placeholder))

  override fun createUpdate() = CustomDrugEntryUpdate()

  override fun createInit() = CustomDrugEntryInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) = effectHandlerFactory.create(this).build()

  override fun events() = Observable
      .mergeArray(
          drugDosageChanges(),
          drugDosageFocusChanges(),
          saveClicks(),
          removeClicks(),
          editFrequencyClicks(),
          imeActionDoneClicks(),
          hotEvents
      ).compose(ReportAnalyticsEvents())
      .cast<CustomDrugEntryEvent>()

  private fun imeActionDoneClicks() = drugDosageEditText.editorActions { it == EditorInfo.IME_ACTION_DONE }.map { ImeActionDoneClicked }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    context.injector<Injector>().inject(this)
  }

  private fun drugDosageChanges() = drugDosageEditText.textChanges(::DosageEdited)

  private fun drugDosageFocusChanges() = drugDosageEditText.focusChanges().map(::DosageFocusChanged)

  private fun editFrequencyClicks() = drugFrequencyEditText.clicks().map { EditFrequencyClicked }

  private fun saveClicks(): Observable<UiEvent> {
    val dosageImeClicks = drugDosageEditText
        .editorActions { it == EditorInfo.IME_ACTION_DONE }
        .map { Unit }

    return saveButton
        .clicks()
        .mergeWith(dosageImeClicks)
        .map { AddMedicineButtonClicked(screenKey.patientUuid) }
  }

  private fun removeClicks(): Observable<UiEvent> =
      removeButton
          .clicks()
          .map { RemoveDrugButtonClicked }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setFragmentResultListener(SelectDrugFrequency) { _, result ->
      if (result is Succeeded)
        hotEvents.onNext(FrequencyEdited(SelectDrugFrequencyDialog.readDrugFrequency(result)))
    }

    drugDosageEditText.imeOptions = EditorInfo.IME_ACTION_DONE
  }

  override fun showEditFrequencyDialog(frequency: DrugFrequency?) {
    router.pushExpectingResult(SelectDrugFrequency, SelectDrugFrequencyDialog.Key(frequency))
  }

  override fun setDrugFrequency(frequencyLabel: String) {
    drugFrequencyEditText.setText(frequencyLabel)
  }

  override fun setSheetTitle(drugName: String?, dosage: String?, frequencyLabel: String) {
    val hasDrugFrequency = frequencyLabel != getString(R.string.custom_drug_entry_sheet_frequency_none)
    val frequency = if (hasDrugFrequency) frequencyLabel else null

    titleTextView.text = listOfNotNull(drugName, dosage, frequency).joinToString()
  }

  override fun showProgressBar() {
    progressIndicator.visibility = VISIBLE
  }

  override fun hideCustomDrugEntryUi() {
    customDrugEntrySheetUiGroup.visibility = GONE
    saveButtonFrame.visibility = GONE
  }

  override fun hideProgressBar() {
    progressIndicator.visibility = GONE
  }

  override fun showCustomDrugEntryUi() {
    customDrugEntrySheetUiGroup.visibility = VISIBLE
    saveButtonFrame.visibility = VISIBLE
  }

  override fun showKeyboard() {
    drugDosageEditText.showKeyboard()
  }

  override fun clearFocusFromDosageEditText() {
    drugDosageEditText.clearFocus()
  }

  override fun setCursorPosition(position: Int) {
    drugDosageEditText.post { drugDosageEditText.setSelection(position) }
  }

  override fun showSaveButtonProgressState() {
    saveButton.setButtonState(InProgress)
  }

  override fun closeSheetAndGoToEditMedicineScreen() {
    router.popUntil(PrescribedDrugsScreenKey(screenKey.patientUuid))
  }

  override fun hideKeyboard() {
    binding.rootLayout.hideKeyboard()
  }

  override fun setDrugDosageText(dosage: String) {
    drugDosageEditText.setText(dosage)
  }

  override fun setDrugDosage(dosage: String?) {
    drugDosageEditText.setText(dosage)
  }

  override fun moveDrugDosageCursorToBeginning() {
    drugDosageEditText.post { drugDosageEditText.setSelection(0) }
  }

  override fun showRemoveButton() {
    removeButton.visibility = VISIBLE
  }

  override fun hideRemoveButton() {
    removeButton.visibility = GONE
  }

  override fun setButtonTextAsSave() {
    saveButton.text = getString(R.string.custom_drug_entry_sheet_save_button_text)
  }

  override fun setButtonTextAsAdd() {
    saveButton.text = getString(R.string.custom_drug_entry_sheet_add_button_text)
  }

  @Parcelize
  data class Key(
      val openAs: OpenAs,
      val patientUuid: UUID,
      override val analyticsName: String = "Custom Drug Entry Sheet"
  ) : ScreenKey() {
    @IgnoredOnParcel
    override val type = ScreenType.Modal

    override fun instantiateFragment() = CustomDrugEntrySheet()
  }

  interface Injector {
    fun inject(target: CustomDrugEntrySheet)
  }

  @Parcelize
  object SelectDrugFrequency : Parcelable
}
