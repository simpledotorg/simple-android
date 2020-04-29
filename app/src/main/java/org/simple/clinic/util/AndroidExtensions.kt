package org.simple.clinic.util

import android.content.Context

inline fun Context.wrap(wrapper: (Context) -> Context): Context = wrapper(this)
