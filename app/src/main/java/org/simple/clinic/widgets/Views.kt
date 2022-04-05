package org.simple.clinic.widgets

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.doOnDetach
import androidx.core.widget.NestedScrollView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.DynamicAnimation.ALPHA
import androidx.dynamicanimation.animation.DynamicAnimation.ROTATION
import androidx.dynamicanimation.animation.DynamicAnimation.ROTATION_X
import androidx.dynamicanimation.animation.DynamicAnimation.ROTATION_Y
import androidx.dynamicanimation.animation.DynamicAnimation.SCALE_X
import androidx.dynamicanimation.animation.DynamicAnimation.SCALE_Y
import androidx.dynamicanimation.animation.DynamicAnimation.SCROLL_X
import androidx.dynamicanimation.animation.DynamicAnimation.SCROLL_Y
import androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_X
import androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_Y
import androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_Z
import androidx.dynamicanimation.animation.DynamicAnimation.X
import androidx.dynamicanimation.animation.DynamicAnimation.Y
import androidx.dynamicanimation.animation.DynamicAnimation.Z
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import org.simple.clinic.R
import timber.log.Timber
import java.time.Duration

fun EditText.showKeyboard() {
  fun openKeyboard() {
    requestFocus()
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
  }

  postDelayed(::openKeyboard, 100)

  doOnDetach { removeCallbacks(::openKeyboard) }
}

fun ViewGroup.hideKeyboard() {
  post {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
  }
}

fun EditText.setTextWithWatcher(textToSet: CharSequence?, textWatcher: TextWatcher) {
  removeTextChangedListener(textWatcher)
  setText(textToSet)

  // Cannot rely on textToSet. It's possible that the
  // EditText modifies the text using InputFilters.
  setSelection(text.length)
  addTextChangedListener(textWatcher)
}

fun EditText.setTextAndCursor(textToSet: CharSequence?) {
  setText(textToSet)

  // Cannot rely on textToSet. It's possible that the
  // EditText modifies the text using InputFilters.
  setSelection(text.length)
}

fun View.setPadding(@DimenRes paddingRes: Int) {
  val padding = resources.getDimensionPixelSize(paddingRes)
  setPaddingRelative(padding, padding, padding, padding)
}

fun View.setPaddingBottom(@DimenRes paddingRes: Int) {
  val newPaddingBottom = resources.getDimensionPixelSize(paddingRes)
  setPaddingRelative(paddingStart, paddingTop, paddingEnd, newPaddingBottom)
}

fun View.setPaddingTop(@DimenRes paddingRes: Int) {
  val newPaddingTop = resources.getDimensionPixelSize(paddingRes)
  setPaddingRelative(paddingStart, newPaddingTop, paddingEnd, paddingBottom)
}

fun View.setHorizontalPadding(@DimenRes paddingRes: Int) {
  val padding = resources.getDimensionPixelSize(paddingRes)
  setPaddingRelative(padding, paddingTop, padding, paddingBottom)
}

fun View.setTopMarginRes(@DimenRes topMarginRes: Int) {
  val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
  marginLayoutParams.topMargin = resources.getDimensionPixelSize(topMarginRes)
  layoutParams = marginLayoutParams
}

fun View.setBottomMarginRes(@DimenRes bottomMarginRes: Int) {
  val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
  marginLayoutParams.bottomMargin = resources.getDimensionPixelSize(bottomMarginRes)
  layoutParams = marginLayoutParams
}

fun View.setTopMargin(topMargin: Int) {
  val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
  marginLayoutParams.topMargin = topMargin
  layoutParams = marginLayoutParams
}

fun TextView.setCompoundDrawableStart(@DrawableRes drawableRes: Int) {
  val drawable = ContextCompat.getDrawable(context, drawableRes)
  setCompoundDrawablesRelativeWithIntrinsicBounds(
      drawable,
      compoundDrawablesRelative[1],
      compoundDrawablesRelative[2],
      compoundDrawablesRelative[3])
}

fun TextView.setCompoundDrawableStart(drawable: Drawable?) {
  setCompoundDrawablesRelativeWithIntrinsicBounds(
      drawable,
      compoundDrawablesRelative[1],
      compoundDrawablesRelative[2],
      compoundDrawablesRelative[3])
}

fun TextView.setCompoundDrawableStartWithTint(
    @DrawableRes drawableRes: Int,
    @ColorRes tintColorRes: Int
) {
  val drawable = ResourcesCompat.getDrawable(resources, drawableRes, context.theme)!!
      .let { source ->
        val drawable = source.mutate()
        val tintColor = ResourcesCompat.getColor(resources, tintColorRes, context.theme)
        DrawableCompat.setTint(drawable, tintColor)
        drawable
      }

  setCompoundDrawableStart(drawable)
}

/**
 * Run a function when a View gets measured and laid out on the screen.
 */
fun View.executeOnNextMeasure(runnable: () -> Unit) {
  if (isInEditMode || isLaidOut) {
    runnable()
    return
  }

  viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
    override fun onPreDraw(): Boolean {
      if (isLaidOut) {
        viewTreeObserver.removeOnPreDrawListener(this)
        runnable()

      } else if (visibility == View.GONE) {
        Timber.w("View's visibility is set to Gone. It'll never be measured: %s", resourceName())
        viewTreeObserver.removeOnPreDrawListener(this)
      }
      return true
    }
  })
}

/**
 * Like [View.getTop], but works even when a View is not the immediate child of [superParent].
 */
fun View.topRelativeTo(superParent: ViewGroup): Int {
  var totalDistance = 0
  var nextView = this

  while (true) {
    totalDistance += nextView.top
    if (nextView.parent == superParent) {
      break
    }
    nextView = nextView.parent as View

    if ((nextView.parent as ViewGroup).id == android.R.id.content) {
      throw AssertionError("${resources.getResourceEntryName(superParent.id)} isn't the parent of ${resources.getResourceEntryName(id)}")
    }
  }

  return totalDistance
}

@Suppress("FoldInitializerAndIfToElvis")
fun ViewGroup.indexOfChildId(@IdRes childId: Int): Int {
  val child = findViewById<View>(childId)
  if (child == null) {
    throw AssertionError("${resources.getResourceEntryName(childId)} isn't a part of ${resources.getResourceEntryName(id)}")
  }
  return indexOfChild(child)
}

fun View.resourceName() = resourceNameForId(resources, id)

fun resourceNameForId(resources: Resources, @IdRes id: Int): String {
  var name = "<nameless>"
  try {
    name = resources.getResourceEntryName(id)
  } catch (e: Resources.NotFoundException) {
    // Nothing to see here
  }

  return name
}

fun View.locationRectOnScreen(): Rect {
  val location = IntArray(2)
  getLocationOnScreen(location)

  val left = location[0]
  val top = location[1]
  return Rect(left, top, left + width, top + height)
}

fun View.locationRectInWindow(): Rect {
  val location = IntArray(2)
  getLocationInWindow(location)

  val left = location[0]
  val top = location[1]
  return Rect(left, top, left + width, top + height)
}

val View.marginLayoutParams: ViewGroup.MarginLayoutParams
  get() = layoutParams as ViewGroup.MarginLayoutParams

fun dpToPx(dp: Float): Float {
  val metrics = Resources.getSystem().displayMetrics
  return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun dpToPx(dp: Int) = dpToPx(dp.toFloat())

fun TextView.setTextAppearanceCompat(@StyleRes resourceId: Int) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    setTextAppearance(resourceId)
  } else {
    @Suppress("DEPRECATION")
    setTextAppearance(context, resourceId)
  }
}

fun ScrollView.scrollToChild(view: View, onScrollComplete: () -> Unit = {}) {
  post {
    val distanceToScrollFromTop = view.topRelativeTo(this)

    smoothScrollTo(0, distanceToScrollFromTop)

    postDelayed(onScrollComplete, 400)
  }

  doOnDetach { removeCallbacks(onScrollComplete) }
}

fun NestedScrollView.scrollToChild(view: View, onScrollComplete: () -> Unit = {}) {
  post {
    val distanceToScrollFromTop = view.topRelativeTo(this)

    smoothScrollTo(0, distanceToScrollFromTop)

    postDelayed(onScrollComplete, 400)
  }

  doOnDetach { removeCallbacks(onScrollComplete) }
}

var ViewFlipper.displayedChildResId: Int
  @IdRes
  get() = getChildAt(displayedChild).id
  set(@IdRes id) {
    val child = findViewById<View>(id)
    val childIndex = indexOfChild(child)
    if (childIndex == -1) {
      throw AssertionError("No child found with ID ${resources.getResourceEntryName(id)}")
    } else {
      displayedChild = childIndex
    }
  }

fun <T> EditText.textChanges(mapper: (String) -> T): Observable<T> {
  return this.textChanges()
      .map { it.toString() }
      .map(mapper)
}

fun View.visibleOrGone(isVisible: Boolean) {
  visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun MenuItem.visibleOrGone(isVisible: Boolean) {
  this.isVisible = isVisible
  this.isEnabled = isVisible
}

fun ViewPropertyAnimator.setDuration(duration: Duration): ViewPropertyAnimator {
  return setDuration(duration.toMillis())
}

fun RadioGroup.checkWithListener(@IdRes id: Int, onCheckedChangeListener: RadioGroup.OnCheckedChangeListener) {
  setOnCheckedChangeListener(null)
  check(id)
  setOnCheckedChangeListener(onCheckedChangeListener)
}

fun View.spring(
    property: DynamicAnimation.ViewProperty,
    damping: Float = SpringForce.DAMPING_RATIO_NO_BOUNCY,
    stiffness: Float = 500f
): SpringAnimation {
  val key = getKey(property)
  var springAnim = getTag(key) as? SpringAnimation?
  if (springAnim == null) {
    springAnim = SpringAnimation(this, property).apply {
      spring = SpringForce().apply {
        this.dampingRatio = damping
        this.stiffness = stiffness
      }
    }
    setTag(key, springAnim)
  }

  return springAnim
}

private fun getKey(property: DynamicAnimation.ViewProperty): Int {
  return when (property) {
    TRANSLATION_X -> R.id.spring_animation_translation_x
    TRANSLATION_Y -> R.id.spring_animation_translation_y
    TRANSLATION_Z -> R.id.spring_animation_translation_z
    SCALE_X -> R.id.spring_animation_scale_x
    SCALE_Y -> R.id.spring_animation_scale_y
    ROTATION -> R.id.spring_animation_rotation
    ROTATION_X -> R.id.spring_animation_rotation_x
    ROTATION_Y -> R.id.spring_animation_rotation_y
    X -> R.id.spring_animation_x
    Y -> R.id.spring_animation_y
    Z -> R.id.spring_animation_z
    ALPHA -> R.id.spring_animation_alpha
    SCROLL_X -> R.id.spring_animation_scrollx
    SCROLL_Y -> R.id.spring_animation_scrollY
    else -> 0
  }
}

val Int.dp: Int
  get() = (Resources.getSystem().displayMetrics.density * this).toInt()

inline val View.isVisible: Boolean
  get() = visibility == View.VISIBLE
