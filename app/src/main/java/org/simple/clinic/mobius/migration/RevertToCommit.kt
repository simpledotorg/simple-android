package org.simple.clinic.mobius.migration

@Retention(AnnotationRetention.SOURCE)
annotation class RevertToCommit(
    val withMessage: String,
    val afterCommitId: String
)
