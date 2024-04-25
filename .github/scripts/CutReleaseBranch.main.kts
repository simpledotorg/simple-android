#!/usr/bin/env kscript

@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r")
@file:CompilerOptions("-jvm-target", "1.8")

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import org.eclipse.jgit.lib.Ref
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import kotlin.system.exitProcess

val isCuttingReleaseBranchEnabled: Boolean = System.getenv("CUT_RELEASE_BRANCH")
    .toBooleanStrictOrNull() ?: false

if (!isCuttingReleaseBranchEnabled) exitProcess(1)

val workingDirectory = File(System.getProperty("user.dir"))
val releaseBranchRegex = Regex("release\\/\\d{4}-\\d{2}-\\d{2}\$")

workingDirectory.run {
  val git = Git.open(this)

  val lastReleaseBranch = git
      .branchList()
      .setListMode(ListMode.ALL)
      .call()
      .map(::branchName)
      .filter { branchName -> branchName.matches(releaseBranchRegex) }
      .maxOrNull() ?: return@run

  val currentDate = LocalDate.now(ZoneId.of("Asia/Kolkata"))
  val lastReleaseDate = releaseDateFromBranch(lastReleaseBranch)
  val expectedReleaseDate = lastReleaseDate.plusWeeks(1)

  if (
      currentDate >= expectedReleaseDate &&
      currentDate.dayOfWeek == DayOfWeek.MONDAY
  ) {
    val branchName = "release/$currentDate"

    git.branchCreate().setName(branchName).call()

    runCommand("git push -u origin $branchName")

    exitProcess(0)
  } else {
    println("We don't have to cut a release branch today")
    exitProcess(1)
  }
}

fun branchName(ref: Ref): String {
  return ref
      .name
      .replace("refs/remotes/origin/", "")
}

fun releaseDateFromBranch(name: String): LocalDate {
  return LocalDate.parse(name.replace("release/", ""))
}

fun File.runCommand(command: String) {
  val parts = command.split("\\s".toRegex())

  ProcessBuilder(*parts.toTypedArray())
      .directory(this)
      .start()
      .run {
        val bufferedReader = inputStream.bufferedReader()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
          println(line)
        }
        waitFor()
      }
}
