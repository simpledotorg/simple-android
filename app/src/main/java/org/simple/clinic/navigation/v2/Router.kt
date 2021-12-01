package org.simple.clinic.navigation.v2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import org.simple.clinic.R
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.util.setFragmentResult

/**
 * Class that maintains a history of screens and is used to perform
 * the backstack changes.
 *
 * Code related to fragment transactions is mostly repurposed from https://github.com/Zhuinden/simple-stack-extensions/blob/0bbf8fd045cece22c838b62243674fb7fe450aa0/fragments/src/main/java/com/zhuinden/simplestackextensions/fragments/DefaultFragmentStateChanger.java.
 **/
class Router(
    private var history: History,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int
) {

  companion object {
    private const val HISTORY_STATE_KEY = "org.simple.clinic.navigation.v2.Router.HISTORY_STATE_KEY"
  }

  constructor(
      history: List<ScreenKey>,
      fragmentManager: FragmentManager,
      @IdRes containerId: Int
  ) : this(
      history = History(history.map(::Normal)),
      fragmentManager = fragmentManager,
      containerId = containerId
  )

  constructor(
      initialScreenKey: ScreenKey,
      fragmentManager: FragmentManager,
      @IdRes containerId: Int
  ) : this(
      history = listOf(initialScreenKey),
      fragmentManager = fragmentManager,
      containerId = containerId
  )

  // Used for posting screen results
  private val handler = Handler(Looper.getMainLooper())

  fun onReady(savedInstanceState: Bundle?) {
    history = savedInstanceState?.getParcelable(HISTORY_STATE_KEY) ?: history

    executeStateChange(history, Direction.Replace, null, null)
  }

  fun onSaveInstanceState(savedInstanceState: Bundle) {
    savedInstanceState.putParcelable(HISTORY_STATE_KEY, history)
  }

  fun clearHistoryAndPush(screenKey: ScreenKey, transactionOptions: TransactionOptions? = null) {
    val newHistory = History(listOf(Normal(screenKey)))

    executeStateChange(newHistory, Direction.Replace, null, transactionOptions)
  }

  fun push(screenKey: ScreenKey, transactionOptions: TransactionOptions? = null) {
    val navRequest = Normal(screenKey)
    if (!history.matchesTop(navRequest)) {
      val newHistory = history.add(navRequest)

      executeStateChange(newHistory, Direction.Forward, null, transactionOptions)
    }
  }

  fun pushExpectingResult(
      requestType: Parcelable,
      key: ScreenKey,
      transactionOptions: TransactionOptions? = null
  ) {
    val navRequest = ExpectingResult(requestType, key)
    if (!history.matchesTop(navRequest)) {
      val newHistory = history.add(navRequest)

      executeStateChange(newHistory, Direction.Forward, null, transactionOptions)
    }
  }

  fun replaceTop(screenKey: ScreenKey, transactionOptions: TransactionOptions? = null) {
    val newHistory = history
        .removeLast()
        .add(Normal(screenKey))

    executeStateChange(newHistory, Direction.Replace, null, transactionOptions)
  }

  fun replaceTopExpectingResult(
      requestType: Parcelable,
      screenKey: ScreenKey,
      transactionOptions: TransactionOptions? = null
  ) {
    val newHistory = history
        .removeLast()
        .add(ExpectingResult(requestType, screenKey))

    executeStateChange(newHistory, Direction.Forward, null, transactionOptions)
  }

  fun replaceHistory(newHistory: History) {
    executeStateChange(newHistory, Direction.Replace, null, null)
  }

  fun currentHistory(): History {
    return history.copy()
  }

  fun pop(transactionOptions: TransactionOptions? = null) {
    val newHistory = history.removeLast()

    executeStateChange(newHistory, Direction.Backward, null, transactionOptions)
  }

  fun popWithResult(result: ScreenResult, transactionOptions: TransactionOptions? = null) {
    val newHistory = history.removeLast()

    executeStateChange(newHistory, Direction.Backward, result, transactionOptions)
  }

  fun popUntil(key: ScreenKey, transactionOptions: TransactionOptions? = null) {
    val newHistory = history.removeUntil(key)

    executeStateChange(newHistory, Direction.Backward, null, transactionOptions)
  }

  fun popUntilInclusive(key: ScreenKey, transactionOptions: TransactionOptions? = null) {
    val newHistory = history.removeUntilInclusive(key)

    executeStateChange(newHistory, Direction.Backward, null, transactionOptions)
  }

  fun replaceKeyOfSameType(
      keyToPush: ScreenKey,
      transactionOptions: TransactionOptions? = null
  ) {
    val newHistory = history
        .removeWhile { screenKey -> !screenKey.matchesScreen(keyToPush) }
        .removeLast() // We need to remove the key which matches this key as well
        .add(Normal(keyToPush))

    executeStateChange(newHistory, Direction.Replace, null, transactionOptions)
  }

  fun hasKeyOfType(clazz: Class<*>): Boolean {
    return history.requests.any { it.key.javaClass == clazz }
  }

  fun onBackPressed(): Boolean {
    val currentTop = history.top()
    val fragment = fragmentManager.findFragmentByTag(currentTop.key.fragmentTag)

    require(fragment != null) { "Could not find fragment for [$currentTop]" }

    return when (fragment) {
      !is HandlesBack -> backPressedWithoutOverriding()
      else -> fragment.onBackPressed() || backPressedWithoutOverriding()
    }
  }

  private fun backPressedWithoutOverriding(): Boolean {
    val areScreensLeftToPop = history.requests.size > 1

    return if (areScreensLeftToPop) {
      pop()
      true
    } else false
  }

  private fun executeStateChange(
      newHistory: History,
      direction: Direction,
      screenResult: ScreenResult?,
      transactionOptions: TransactionOptions?
  ) {
    checkNotMainThread()

    fragmentManager.executePendingTransactions()

    val transaction = fragmentManager
        .beginTransaction()
        .disallowAddToBackStack()

    val (enterAnim, exitAnim) = getTransactionAnimations(direction, transactionOptions)

    transaction.setCustomAnimations(enterAnim, exitAnim)

    val currentNavRequests = history.requests
    val newNavRequests = newHistory.requests
    val newTopScreen = newNavRequests.last()
    val currentTopScreen = currentNavRequests.last()
    val lastFullScreen = newNavRequests
        .dropLast(1) // Ignoring current top screen
        .lastOrNull { !it.key.isModal }

    clearCurrentHistory(
        currentNavRequests = currentNavRequests,
        newNavRequests = newNavRequests,
        transaction = transaction,
        newTopScreen = newTopScreen,
        lastFullScreen = lastFullScreen
    )

    addNewHistory(
        newNavRequests = newNavRequests,
        newTopScreen = newTopScreen,
        transaction = transaction,
        lastFullScreen = lastFullScreen
    )

    transaction.commitNow()

    history = newHistory

    dispatchScreenResult(currentTopScreen, screenResult)
    reportScreenChangeToAnalytics(outgoing = currentTopScreen.key, incoming = newTopScreen.key)
  }

  private fun reportScreenChangeToAnalytics(
      outgoing: ScreenKey,
      incoming: ScreenKey
  ) {
    val outgoingAnalyticsName = if (outgoing == incoming)
      "" // This is the initial screen setup
    else
      outgoing.analyticsName

    val incomingAnalyticsName = incoming.analyticsName

    Analytics.reportScreenChange(outgoingAnalyticsName, incomingAnalyticsName)
  }

  private fun clearCurrentHistory(
      currentNavRequests: List<NavRequest>,
      newNavRequests: List<NavRequest>,
      transaction: FragmentTransaction,
      newTopScreen: NavRequest,
      lastFullScreen: NavRequest?
  ) {
    // Remove old fragments if they are no longer present in the new set of screens
    currentNavRequests.forEach { navRequest ->
      val fragment = fragmentManager.findFragmentByTag(navRequest.key.fragmentTag)

      if (fragment != null) {
        hideOrRemoveFragment(
            newNavRequests = newNavRequests,
            navRequest = navRequest,
            transaction = transaction,
            fragment = fragment,
            newTopScreen = newTopScreen,
            lastFullScreen = lastFullScreen
        )
      }
    }
  }

  private fun hideOrRemoveFragment(
      newNavRequests: List<NavRequest>,
      navRequest: NavRequest,
      transaction: FragmentTransaction,
      fragment: Fragment,
      newTopScreen: NavRequest,
      lastFullScreen: NavRequest?
  ) {
    if (!newNavRequests.contains(navRequest))
      transaction.remove(fragment)
    else if (fragment.isShowing)
      hideFragment(
          navRequest = navRequest,
          newTopScreen = newTopScreen,
          transaction = transaction,
          fragment = fragment,
          lastFullScreen = lastFullScreen
      )
  }

  private fun hideFragment(
      navRequest: NavRequest,
      newTopScreen: NavRequest,
      transaction: FragmentTransaction,
      fragment: Fragment,
      lastFullScreen: NavRequest?
  ) {
    val isNeitherTopNorLastFullScreen = navRequest != lastFullScreen && navRequest != newTopScreen
    val isLastFullScreenCompletelyObscured = navRequest == lastFullScreen && !newTopScreen.key.isModal

    // Don't hide the last full screen that is behind a modal.
    if (isNeitherTopNorLastFullScreen || isLastFullScreenCompletelyObscured) {
      transaction.detach(fragment)
    }
  }

  private fun addNewHistory(
      newNavRequests: List<NavRequest>,
      newTopScreen: NavRequest,
      transaction: FragmentTransaction,
      lastFullScreen: NavRequest?
  ) {
    newNavRequests.forEach { navRequest ->
      val existingFragment = fragmentManager.findFragmentByTag(navRequest.key.fragmentTag)
      when (navRequest) {
        newTopScreen -> handleAddingTopFragment(navRequest, existingFragment, transaction)
        else -> handleRemovingOlderFragments(newTopScreen, existingFragment, transaction, navRequest, lastFullScreen)
      }
    }
  }

  private fun handleAddingTopFragment(
      navRequest: NavRequest,
      existingFragment: Fragment?,
      transaction: FragmentTransaction
  ) {
    when {
      navRequest.key.isModal -> handleAddingModalTopFragment(existingFragment, transaction, navRequest)
      else -> handleAddingFullscreenTopFragment(existingFragment, transaction, navRequest)
    }
  }

  private fun handleAddingFullscreenTopFragment(
      existingFragment: Fragment?,
      transaction: FragmentTransaction,
      navRequest: NavRequest
  ) {
    when {
      existingFragment != null -> attachOrReplaceTop(existingFragment, transaction, navRequest)
      else -> transaction.add(containerId, navRequest.key.createFragment(), navRequest.key.fragmentTag)
    }
  }

  private fun attachOrReplaceTop(
      existingFragment: Fragment,
      transaction: FragmentTransaction,
      navRequest: NavRequest
  ) {
    when {
      // fragments are quirky, they die asynchronously. Ignore if they're still there.
      existingFragment.isRemoving -> transaction.replace(containerId, navRequest.key.createFragment(), navRequest.key.fragmentTag)
      existingFragment.isNotShowing -> transaction.attach(existingFragment)
    }
  }

  private fun handleAddingModalTopFragment(
      existingFragment: Fragment?,
      transaction: FragmentTransaction,
      navRequest: NavRequest
  ) {
    when {
      existingFragment != null -> attachOrAddTop(existingFragment, transaction, navRequest)
      else -> transaction.add(navRequest.key.createFragment(), navRequest.key.fragmentTag)
    }
  }

  private fun attachOrAddTop(
      existingFragment: Fragment,
      transaction: FragmentTransaction,
      navRequest: NavRequest
  ) {
    when {
      // fragments are quirky, they die asynchronously. Ignore if they're still there.
      existingFragment.isRemoving -> transaction.add(navRequest.key.createFragment(), navRequest.key.fragmentTag)
      existingFragment.isNotShowing -> transaction.attach(existingFragment)
    }
  }

  private fun handleRemovingOlderFragments(
      newTopScreen: NavRequest,
      existingFragment: Fragment?,
      transaction: FragmentTransaction,
      navRequest: NavRequest,
      lastFullScreen: NavRequest?
  ) {
    when {
      newTopScreen.key.isModal -> handleRemovingOlderFragmentForModalTop(navRequest,
          lastFullScreen,
          existingFragment,
          transaction)
      else -> handleRemovingOlderFragmentForFullscreenTop(existingFragment, transaction)
    }
  }

  private fun handleRemovingOlderFragmentForFullscreenTop(
      existingFragment: Fragment?,
      transaction: FragmentTransaction
  ) {
    if (existingFragment != null && existingFragment.isShowing) {
      transaction.detach(existingFragment)
    }
  }

  private fun handleRemovingOlderFragmentForModalTop(
      navRequest: NavRequest,
      lastFullScreen: NavRequest?,
      existingFragment: Fragment?,
      transaction: FragmentTransaction
  ) {
    // We want to show the last full screen behind modal(s).
    // Even if there are multiple modals, we show the last full screen and add modals on top of that.
    when {
      navRequest == lastFullScreen || !history.isNavRequestBeforeAnother(navRequest, lastFullScreen) -> ensureFragmentIsPresent(existingFragment, transaction, navRequest)
      existingFragment != null && existingFragment.isShowing -> transaction.detach(existingFragment)
    }
  }

  private fun ensureFragmentIsPresent(
      existingFragment: Fragment?,
      transaction: FragmentTransaction,
      navRequest: NavRequest
  ) {
    when {
      existingFragment == null -> transaction.replace(containerId, navRequest.key.createFragment(), navRequest.key.fragmentTag)
      existingFragment.isRemoving -> transaction.add(navRequest.key.createFragment(), navRequest.key.fragmentTag)
      else -> transaction.attach(existingFragment)
    }
  }

  private fun dispatchScreenResult(
      currentTopScreen: NavRequest,
      screenResult: ScreenResult?
  ) {
    if (currentTopScreen is ExpectingResult && screenResult != null) {
      val requestType = currentTopScreen.requestType

      fragmentManager.setFragmentResult(requestType, screenResult)
    }
  }

  private fun checkNotMainThread() {
    if (Looper.getMainLooper() !== Looper.myLooper()) {
      throw RuntimeException("Can only execute navigation state changes on the UI thread! Current thread is [${Thread.currentThread().name}].")
    }
  }

  private fun getTransactionAnimations(
      direction: Direction,
      transactionOptions: TransactionOptions?
  ) = when (direction) {
    Direction.Forward -> Pair(
        transactionOptions?.enterAnim ?: R.anim.router_slide_in_right,
        transactionOptions?.exitAnim ?: R.anim.router_slide_out_left
    )
    Direction.Backward -> Pair(
        transactionOptions?.enterAnim ?: R.anim.router_slide_in_left,
        transactionOptions?.exitAnim ?: R.anim.router_slide_out_right
    )
    Direction.Replace -> Pair(
        transactionOptions?.enterAnim ?: R.anim.router_fade_in,
        transactionOptions?.exitAnim ?: R.anim.router_fade_out
    )
  }
}

private val Fragment.isShowing: Boolean
  get() = !isDetached

private val Fragment.isNotShowing: Boolean
  get() = isDetached
