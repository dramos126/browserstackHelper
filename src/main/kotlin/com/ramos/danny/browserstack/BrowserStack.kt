package com.ramos.danny.browserstack

import com.ramos.danny.browserstack.response.BuildRequest
import com.ramos.danny.client
import com.ramos.danny.utils.toJsonObject
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class BrowserStack {
  private lateinit var browserStackBuild: BrowserStackBuild
  private val failedSessions = mutableListOf<DeviceSessions>()
  private val failedTests = mutableListOf<String>()

  suspend fun runFailedTests(buildId: String, framework: String, shards: Int): Pair<HttpStatusCode, String> {
    this.browserStackBuild = getBuild(buildId, framework)
    filterFailedTests()
    createRetryRequestObject(shards)

    return newBuildRequest(framework, shards)
  }

  suspend fun buildResults(id: String, framework: String): JsonObject {
    val build = getBuild(id, framework)
    var totalTestCount = 0
    var totalPassingCount = 0

    build.devices.forEach { device ->
      device.sessions.forEach { session ->
        totalTestCount += session.testcases.count
        totalPassingCount += session.testcases.status.passed
      }
    }

    return mapOf(
      "passed" to totalPassingCount,
      "total" to totalTestCount
    ).toJsonObject()
  }

  suspend fun newBuildRequest(
    framework: String,
    shards: Int,
    body: JsonElement? = null
  ): Pair<HttpStatusCode, String> {
    val url = URLBuilder(
      URLProtocol.HTTPS,
      hostURL,
      pathSegments = listOf(appAutomate, framework, apiVersion, build)
    ).buildString()

    val response: BuildRequest = client.post(url) {
      headers { append(HttpHeaders.ContentType, "application/json") }
      setBody(body ?: createRetryRequestObject(shards))
    }.body()

    return if (response.build_id.isNullOrBlank()) {
      Pair(HttpStatusCode.InternalServerError, "")
    } else {
      Pair(HttpStatusCode.OK, response.build_id)
    }
  }

  private suspend fun getBuild(id: String, framework: String): BrowserStackBuild {
    val buildURL = URLBuilder(
      URLProtocol.HTTPS,
      hostURL,
      pathSegments = listOf(appAutomate, framework, apiVersion, builds, id)
    ).buildString()

    return client.get(buildURL).body()
  }

  private fun createRetryRequestObject(shards: Int): JsonElement {
    val requestObject = mapOf(
      "shards" to createShardsObject(shards),
      "app" to browserStackBuild.input_capabilities.app,
      "testSuite" to browserStackBuild.input_capabilities.testSuite,
      "testSuite" to browserStackBuild.input_capabilities.testSuite,
      "buildTag" to "${browserStackBuild.input_capabilities.buildTag}_retry",
      "project" to browserStackBuild.input_capabilities.project,
      "networkLogs" to browserStackBuild.input_capabilities.networkLogs,
      "deviceLogs" to browserStackBuild.input_capabilities.deviceLogs,
      "debugscreenshots" to browserStackBuild.input_capabilities.debugscreenshots,
      "devices" to browserStackBuild.input_capabilities.devices,
      "setEnvVariables" to mapOf(
        "IS_BROWSERSTACK" to browserStackBuild.input_capabilities.setEnvVariables.IS_BROWSERSTACK,
        "BUILD_NUMBER" to browserStackBuild.input_capabilities.setEnvVariables.BUILD_NUMBER
      )
    )
    return requestObject.toJsonObject()
  }

  private fun createShardsObject(shards: Int): Map<String, Any> {
    val mapping = mutableListOf<Any>()
    val chunckedBy = if (failedTests.size % shards != 0) {
      failedTests.size / shards + 1
    } else {
      failedTests.size / shards
    }
    val listOfFailedTestLists = failedTests.chunked(chunckedBy)

    for (i in 0 until shards) {
      val shard = mapOf(
        "name" to "Shard $i",
        "strategy" to "only-testing",
        "values" to listOfFailedTestLists[i]
      )
      mapping.add(shard)
    }

    return mapOf<String, Any>(
      "numberOfShards" to shards,
      "deviceSelection" to "any",
      "mapping" to mapping
    )
  }

  private suspend fun filterFailedTests() {
    filterFailedSessions()
    failedSessions.forEach { deviceSession ->
      val url = URLBuilder(
        URLProtocol.HTTPS,
        hostURL,
        pathSegments = listOf(
          appAutomate,
          browserStackBuild.framework,
          apiVersion,
          builds,
          browserStackBuild.id,
          buildSessions,
          deviceSession.id
        )
      ).buildString()

      val session: Session = client.get(url).body()
      session.testcases.data.forEach { data ->
        data.testcases.forEach { testCase ->
          if (testCase.status != passed) {
            failedTests.add("${data.`class`}/${testCase.name}")
          }
        }
      }
    }
  }

  private fun filterFailedSessions() {
    browserStackBuild.devices.forEach { device ->
      device.sessions.forEach { session ->
        if (session.status != passed) {
          failedSessions.add(session)
        }
      }
    }
  }
}
