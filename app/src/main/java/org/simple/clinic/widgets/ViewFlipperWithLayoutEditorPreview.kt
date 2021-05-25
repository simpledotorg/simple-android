package org.simple.clinic.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ViewFlipper
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.simple.clinic.R

/** Exposes a way to set the displayed child in layout preview. */
class ViewFlipperWithLayoutEditorPreview(
    context: Context,
    attrs: AttributeSet
) : ViewFlipper(context, attrs) {

  private var childToDisplayPostInflate: Int = 0

  private val displayedChildChangesSubject: Subject<Int> = BehaviorSubject.create()
  val displayedChildChanges: Observable<Int> = displayedChildChangesSubject.hide()

  init {
    if (isInEditMode) {
      val attributes = context.obtainStyledAttributes(attrs, R.styleable.ViewFlipperWithLayoutEditorPreview)
      childToDisplayPostInflate = attributes.getInt(R.styleable.ViewFlipperWithLayoutEditorPreview_debug_displayedChild, 0)
      attributes.recycle()
    }
    displayedChildChangesSubject.onNext(displayedChild)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      if (childToDisplayPostInflate >= childCount) {
        throw IllegalStateException("displayed child index is greater than child count")
      }
      displayedChild = childToDisplayPostInflate
    }
  }

  override fun setDisplayedChild(whichChild: Int) {
    super.setDisplayedChild(whichChild)
    displayedChildChangesSubject.onNext(whichChild)
  }
}
