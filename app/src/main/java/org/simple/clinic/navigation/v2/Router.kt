package org.simple.clinic.navigation.v2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import org.simple.clinic.platform.analytics.Analytics

/**
 * Class that maintains a history of screens and is used to perform
 * the backstack changes.
 *
 * Code related to fragment transactions is mostly repurposed from https://github.com/Zhuinden/simple-stack-extensions/blob/0bbf8fd045cece22c838b62243674fb7fe450aa0/fragments/src/main/java/com/zhuinden/simplestackextensions/fragments/DefaultFragmentStateChanger.java.
 **/
class Router(
    private var history: History,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val savedInstanceState: Bundle?
) {

  companion object {
    private const val HISTORY_STATE_KEY = "org.simple.clinic.navigation.v2.Router.HISTORY_STATE_KEY"
  }

  constructor(
      history: List<ScreenKey>,
      fragmentManager: FragmentManager,
      @IdRes containerId: Int,
      savedInstanceState: Bundle?
  ) : this(
      history = History(history.map(::Normal)),
      fragmentManager = fragmentManager,
      containerId = containerId,
      savedInstanceState = savedInstanceState
  )

  constructor(
      initialScreenKey: ScreenKey,
      fragmentManager: FragmentManager,
      @IdRes containerId: Int,
      savedInstanceState: Bundle?
  ) : this(
      history = listOf(initialScreenKey),
      fragmentManager = fragmentManager,
      containerId = containerId,
      savedInstanceState = savedInstanceState
  )

  // Used for posting screen results
  private val handler = Handler(Looper.getMainLooper())

  init {
    history = savedInstanceState?.getParcelable(HISTORY_STATE_KEY) ?: history

    executeStateChange(history, Direction.Replace, null)
  }

  fun onSaveInstanceState(savedInstanceState: Bundle) {
    savedInstanceState.putParcelable(HISTORY_STATE_KEY, history)
  }

  fun push(screenKey: ScreenKey) {
    val newHistory = history.add(Normal(screenKey))

    executeStateChange(newHistory, Direction.Forward, null)
  }

  fun pushExpectingResult(
      requestType: Parcelable,
      key: ScreenKey
  ) {
    val newHistory = history.add(ExpectingResult(requestType, key))

    executeStateChange(newHistory, Direction.Forward, null)
  }

  fun pop() {
    val newHistory = history.removeLast()

    executeStateChange(newHistory, Direction.Backward, null)
  }

  fun popWithResult(result: ScreenResult) {
    val newHistory = history.removeLast()

    executeStateChange(newHistory, Direction.Backward, result)
  }

  fun popUntil(key: ScreenKey) {
    val newHistory = history.removeUntil(key)

    executeStateChange(newHistory, Direction.Backward, null)
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
      screenResult: ScreenResult?
  ) {
    checkNotMainThread()

    fragmentManager.executePendingTransactions()

    val transaction = fragmentManager
        .beginTransaction()
        .disallowAddToBackStack()

    transaction.setTransition(when (direction) {
      Direction.Forward -> FragmentTransaction.TRANSIT_FRAGMENT_OPEN
      Direction.Backward -> FragmentTransaction.TRANSIT_FRAGMENT_CLOSE
      Direction.Replace -> FragmentTransaction.TRANSIT_FRAGMENT_FADE
    })

    val currentNavRequests = history.requests
    val newNavRequests = newHistory.requests
    val newTopScreen = newNavRequests.last()
    val currentTopScreen = currentNavRequests.last()
    val lastButOneScreen = if (newNavRequests.size > 1) newNavRequests[newNavRequests.lastIndex - 1] else null

    clearCurrentHistory(
        currentNavRequests = currentNavRequests,
        newNavRequests = newNavRequests,
        transaction = transaction,
        newTopScreen = newTopScreen,
        lastButOneScreen = lastButOneScreen
    )

    addNewHistory(
        newNavRequests = newNavRequests,
        newTopScreen = newTopScreen,
        transaction = transaction,
        lastButOneScreen = lastButOneScreen
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
      lastButOneScreen: NavRequest?
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
            lastButOneScreen = lastButOneScreen
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
      lastButOneScreen: NavRequest?
  ) {
    if (!newNavRequests.contains(navRequest))
      transaction.remove(fragment)
    else if (fragment.isShowing)
      hideFragment(
          navRequest = navRequest,
          newTopScreen = newTopScreen,
          transaction = transaction,
          fragment = fragment,
          lastButOneScreen = lastButOneScreen
      )
  }

  private fun hideFragment(
      navRequest: NavRequest,
      newTopScreen: NavRequest,
      transaction: FragmentTransaction,
      fragment: Fragment,
      lastButOneScreen: NavRequest?
  ) {
    val isNeitherTopNorLastButOneScreen = navRequest != newTopScreen && navRequest != lastButOneScreen
    val isLastButOneScreenCompletelyObscured = navRequest == lastButOneScreen && !newTopScreen.key.isModal

    // Don't hide the last but one screen if the incoming screen is a modal since we want whatever changes the user has
    // made in this screen to be visible in the background of the modal
    if (isNeitherTopNorLastButOneScreen || isLastButOneScreenCompletelyObscured) {
      transaction.detach(fragment)
    }
  }

  private fun addNewHistory(
      newNavRequests: List<NavRequest>,
      newTopScreen: NavRequest,
      transaction: FragmentTransaction,
      lastButOneScreen: NavRequest?
  ) {
    newNavRequests.forEach { navRequest ->
      val existingFragment = fragmentManager.findFragmentByTag(navRequest.key.fragmentTag)
      when (navRequest) {
        newTopScreen -> handleAddingTopFragment(navRequest, existingFragment, transaction)
        else -> handleRemovingOlderFragments(newTopScreen, existingFragment, transaction, navRequest, lastButOneScreen)
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
      lastButOneScreen: NavRequest?
  ) {
    when {
      newTopScreen.key.isModal -> handleRemovingOlderFragmentForModalTop(navRequest, lastButOneScreen, existingFragment, transaction)
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
      lastButOneScreen: NavRequest?,
      existingFragment: Fragment?,
      transaction: FragmentTransaction
  ) {
    // Last but one key should not be detached since the topmost key is a modal
    // and we want the previous screen to be visible
    when {
      navRequest == lastButOneScreen -> ensureFragmentIsPresent(existingFragment, transaction, navRequest)
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
    val newTopNavRequest = history.top()
    if (currentTopScreen is ExpectingResult && screenResult != null) {
      val targetFragment = fragmentManager.findFragmentByTag(newTopNavRequest.key.fragmentTag)

      require(targetFragment != null) { "Could not find fragment for key: [${newTopNavRequest.key}]" }
      require(targetFragment is ExpectsResult) { "Key [${newTopNavRequest.key}] was pushed expecting results, but fragment [${targetFragment.javaClass.name}] does not implement [${ExpectsResult::class.java.name}]!" }

      handler.post { targetFragment.onScreenResult(currentTopScreen.requestType, screenResult) }
    }
  }

  private fun checkNotMainThread() {
    if (Looper.getMainLooper() !== Looper.myLooper()) {
      throw RuntimeException("Can only execute navigation state changes on the UI thread! Current thread is [${Thread.currentThread().name}].")
    }
  }
}

private val Fragment.isShowing: Boolean
  get() = !isDetached

private val Fragment.isNotShowing: Boolean
  get() = isDetached
