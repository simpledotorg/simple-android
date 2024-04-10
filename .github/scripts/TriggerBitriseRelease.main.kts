#!/usr/bin/env kscript

@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("com.squareup.okhttp3:okhttp:4.12.0")
@file:CompilerOptions("-jvm-target", "1.8")

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.system.exitProcess

val branchRef = args.firstOrNull().orEmpty()
val accessToken = args[1]
val appSlug = args[2]
val workflowId = args[3]

val formattedReleaseBranch = branchRef.replace("refs/heads/", "")
val releaseBranchRegex = Regex("release\\/\\d{4}-\\d{2}-\\d{2}\$")

println("Checking for newly created release branch")

if (formattedReleaseBranch.matches(releaseBranchRegex)) {
  println("Newly created release branch found: $formattedReleaseBranch")

  val client = OkHttpClient.Builder().build()

  val postBody = """
    {
        "hook_info": {
          "type": "bitrise"
        },
        "build_params": {
          "branch": "$formattedReleaseBranch",
          "workflow_id":"$workflowId"
        }
    }
  """

  val request = Request.Builder()
      .addHeader("Authorization", accessToken)
      .url("https://api.bitrise.io/v0.1/apps/$appSlug/builds")
      .post(postBody.toRequestBody("application/json".toMediaType()))
      .build()

  val response = client.newCall(request = request).execute()

  println(response.body!!.string())

  exitProcess(0)
} else {
  println("$formattedReleaseBranch is not a release branch")
  exitProcess(1)
}
