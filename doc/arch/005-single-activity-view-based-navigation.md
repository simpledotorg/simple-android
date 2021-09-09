# ADR 005: A single activity, view-based screen navigation system

## Status

Superceded by [011](./011-screen-navigation-v2.md) on 2020-01-12.

## Context

Fragments were introduced in Android API 11, as a way to model behavior or a portion of the user interface. Fragments can be shared between multiple
Activities, and can be replaced by other Fragments without affecting the parent Activity.

However, Fragments have a lifecycle that, although part of the Activity lifecycle, is slightly different and the two are not sychronous. This has
created a class of bugs, of which many are common in Android apps; e.g.:

- Fragments are usually created after `Activity#onCreate()` has been called. But during state restoration, Fragments get created
  in `Activity#onCreate()` when the Activity’s properties may not have been initialised.
- When exchanging data between Activities, the `Activity#onActivityResult()` gets called before the Activity is resumed, and creating a Fragment when
  a result is received will crash the app.
- Fragments are not resumed when `Activity#onResume()` is called. The documentation recommends using `Activty#onResumeFragments()` instead.
- Fragment lifecycle events are not synchronised with Activity lifecycle, and trying to do this introduces an extra layer of complexity. Race
  conditions are difficult to avoid with an asynchronous lifecycle
  causing [pain, suffering and misery](https://www.google.com/search?q=illegal+state+exception+fragment+android).

## Decision

We will use a single `Activity` to hold all our “screens”, and entirely avoid using `Fragment` classes. To switch between “screens”, we will
use [Flow](https://github.com/square/flow) — a library that enables navigating between different UI states.

Flow makes it easy to construct screens using just `View` classes. Views are inflated synchronously, which drastically reduces complexity and
eliminates race conditions. When we inflate a layout XML using `LayoutInflater#inflate()`, we are guaranteed that the View will be ready for making
any modifications. A single `Activity` also allows for easy animations, because inter-activity transitions are harder to get right.

Since we are using `View` classes as screens, we also lose the ability to use
the [Back Stack](https://developer.android.com/guide/components/activities/tasks-and-back-stack). Flow also helps with this by providing a `View`
-based backstack.

## Consequences

Using a single `Activity` to hold all views, and using Flow for navigation is quite unusual. Flow is an external library, written and maintained by a
third-party — it is open source, but if the library gets neglected, we will have to take up the task of maintaining it. Using `Fragment`
and `Activity` classes is so common in the community that help is easy to find and obtain. If we run into any roadblocks with Flow, we will not find
the same degree of support.

Android also provides a way to share data between Activities using `Activity#startActivityForResult()`. We cannot use this in our architecture because
we only have one `Activity`, and out of the box, Flow does not provide a way to share data between screens. We have built our own mechanism to achieve
this which will have to be maintained by us.
