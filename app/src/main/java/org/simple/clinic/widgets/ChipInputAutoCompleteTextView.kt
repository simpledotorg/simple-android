package org.simple.clinic.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.os.bundleOf
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.jakewharton.rxbinding3.InitialValueObservable
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.databinding.ViewChipTextInputBinding
import org.simple.clinic.util.resolveColor

class ChipInputAutoCompleteTextView(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), AdapterView.OnItemClickListener {

  private var binding: ViewChipTextInputBinding? = null

  private val rootView
    get() = binding!!.inputViewRoot

  private val flowHelper
    get() = binding!!.flowHelper

  private val autoCompleteTextView
    get() = binding!!.autoCompleteTextView

  private val inputs = mutableListOf<Input>()
  private val _inputChanges = BehaviorSubject.create<List<String>>()
  val inputChanges: Observable<List<String>>
    get() = _inputChanges

  private val disposable = CompositeDisposable()

  init {
    binding = ViewChipTextInputBinding.inflate(LayoutInflater.from(context), this, true)

    var hint: String? = null
    context.obtainStyledAttributes(attrs, R.styleable.ChipInputAutoCompleteTextView).use { typedArray ->
      hint = typedArray.getString(R.styleable.ChipInputAutoCompleteTextView_hint).orEmpty()
    }
    autoCompleteTextView.hint = hint

    disposable.add(inputChanges(hint))
  }

  override fun onDetachedFromWindow() {
    binding = null
    disposable.clear()
    super.onDetachedFromWindow()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    // Focus edit text if we click outside the text field
    rootView.setOnClickListener { showKeyboard() }

    handleEditTextBackPress()
    handleEditTextImeAction()

    dropDownConfig()
  }

  override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    val item = parent?.getItemAtPosition(position) as? String
    if (item != null) {
      autoCompleteTextView.text.clear()
      addChip(item)
    }
  }

  fun showKeyboard() {
    autoCompleteTextView.showKeyboard()
  }

  fun searchQueryChanges(): InitialValueObservable<CharSequence> {
    return autoCompleteTextView.textChanges()
  }

  fun setInputs(inputs: List<String>) {
    inputs
        .map {
          Input(
              id = View.generateViewId(),
              text = it
          )
        }
        .forEach(::addChipInternal)

    rootView.requestLayout()
    notifyInputChanges()
  }

  fun setAdapter(adapter: ArrayAdapter<String>) {
    autoCompleteTextView.setAdapter(adapter)
  }

  fun setDropdownAnchor(@IdRes id: Int) {
    autoCompleteTextView.dropDownAnchor = id
  }

  private fun handleEditTextBackPress() {
    autoCompleteTextView.setOnKeyListener { _, keyCode, event ->
      val canRemoveLastChip = keyCode == KeyEvent.KEYCODE_DEL &&
          event.action == KeyEvent.ACTION_DOWN &&
          autoCompleteTextView.text.isNullOrBlank()

      if (canRemoveLastChip) {
        removeLastChip()
        return@setOnKeyListener true
      }
      return@setOnKeyListener false
    }
  }

  private fun handleEditTextImeAction() {
    autoCompleteTextView.imeOptions = EditorInfo.IME_ACTION_SEARCH
    autoCompleteTextView.inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS or EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME
    autoCompleteTextView.setHorizontallyScrolling(true)
    autoCompleteTextView.maxLines = 1

    autoCompleteTextView.setOnEditorActionListener { _, actionId, event ->
      val canAddSearchInput = actionId == EditorInfo.IME_ACTION_SEARCH && !autoCompleteTextView.text.isNullOrBlank()

      if (canAddSearchInput) {
        val item = autoCompleteTextView.text.toString()
        autoCompleteTextView.text.clear()
        addChip(item)
        return@setOnEditorActionListener true
      }

      return@setOnEditorActionListener false
    }
  }

  private fun inputChanges(hint: String?) = _inputChanges
      .subscribe {
        if (it.isEmpty()) {
          autoCompleteTextView.hint = hint
        } else {
          autoCompleteTextView.hint = null
        }
      }

  private fun removeLastChip() {
    val lastInput = inputs.last()
    val lastChip = rootView
        .children
        .filterIsInstance<Chip>()
        .firstOrNull { it.id == lastInput.id }

    if (lastChip != null) {
      removeChip(lastChip)
    }
  }

  private fun addChip(item: String) {
    val input = Input(
        id = View.generateViewId(),
        text = item
    )
    addChipInternal(input)

    rootView.requestLayout()
    notifyInputChanges()
  }

  private fun addChipInternal(input: Input) {
    val colorPrimary = context.resolveColor(attrRes = R.attr.colorPrimary)
    val colorOnPrimary = context.resolveColor(attrRes = R.attr.colorOnPrimary)

    val chip = Chip(context).apply {
      id = input.id
      isCloseIconVisible = true
      text = input.text
      chipBackgroundColor = ColorStateList.valueOf(colorPrimary)
      setTextColor(ColorStateList.valueOf(colorOnPrimary))
      closeIconTint = ColorStateList.valueOf(colorOnPrimary)
      setEnsureMinTouchTargetSize(false)
      tag = input
    }

    chip.setOnCloseIconClickListener { _ ->
      // Remove chip from parent view and flow helper
      removeChip(chip)
    }

    // Add chip to parent view first and then update reference ids of flow helper
    rootView.addView(chip)

    // Puts newly added chip and text input at the end
    flowHelper.referencedIds = flowHelper.referencedIds.filter { it != autoCompleteTextView.id }.toIntArray() +
        intArrayOf(chip.id) + intArrayOf(autoCompleteTextView.id)

    inputs.add(input)
  }

  private fun removeChip(chip: Chip) {
    val input = chip.tag as Input

    flowHelper.removeView(chip)
    rootView.removeView(chip)
    inputs.remove(input)

    notifyInputChanges()
  }

  private fun notifyInputChanges() {
    _inputChanges.onNext(inputs.map { it.text })
  }

  private fun dropDownConfig() {
    autoCompleteTextView.onItemClickListener = this
    autoCompleteTextView.dropDownWidth = MATCH_PARENT
    autoCompleteTextView.setDropDownBackgroundDrawable(ColorDrawable(Color.WHITE))
  }

  override fun onSaveInstanceState(): Parcelable {
    return bundleOf(
        "superState" to super.onSaveInstanceState(),
        "inputs" to inputs
    )
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state !is Bundle) {
      super.onRestoreInstanceState(state)
      return
    }

    val superState = state.getParcelable<Parcelable>("superState")
    val restoredInputs = state.getParcelableArrayList<Input>("inputs").orEmpty()

    super.onRestoreInstanceState(superState)

    restoredInputs.forEach(::addChipInternal)

    rootView.requestLayout()
    notifyInputChanges()
  }

  @Parcelize
  private data class Input(val id: Int, val text: String) : Parcelable
}
