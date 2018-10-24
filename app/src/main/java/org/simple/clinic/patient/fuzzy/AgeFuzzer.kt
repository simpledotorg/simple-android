package org.simple.clinic.patient.fuzzy

interface AgeFuzzer {

  fun bounded(age: Int): BoundedAge
}
