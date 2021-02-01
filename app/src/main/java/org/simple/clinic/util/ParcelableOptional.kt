package org.simple.clinic.util

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * We already have an [Optional] class that we use for representing
 * optional values. However, that has a major drawback on Android:
 *
 * - We cannot use it in any class that implements the [Parcelable]
 * interface since it itself is not [Parcelable].
 *
 * In addition, we cannot make the class [Parcelable] because that would
 * force every user of the class to also implement the interface.
 *
 * This class is based on the Java [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html)
 * class, but which forces the [Parcelable] interface on its types.
 **/
data class ParcelableOptional<T : Parcelable>(
    private val value: T?
) : Parcelable {

  companion object {
    fun <T : Parcelable> of(value: T?) = ParcelableOptional<T>(value)
  }

  fun get(): T {
    if (value == null) throw NoSuchElementException("value is not present!")

    return value
  }

  fun isPresent(): Boolean = value != null

  fun isEmpty(): Boolean = value == null

  fun ifPresent(consumer: (T) -> Unit) {
    if (value != null) {
      consumer.invoke(value)
    }
  }

  fun orElse(other: T): T {
    return value ?: other
  }

  fun orElse(failureSupplier: () -> Throwable): T {
    if (value == null) throw failureSupplier()

    return value
  }

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(dest: Parcel?, flags: Int) {

  }
}

fun <T : Parcelable> Optional<T>.parcelable(): ParcelableOptional<T> {
  return ParcelableOptional<T>(this.toNullable())
}
