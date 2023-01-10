package com.ramos.danny.browserstack

import com.ramos.danny.browserstack.response.BuildRequest
import com.ramos.danny.client
import com.ramos.danny.utils.toJsonElement
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class BrowserStack {
  private lateinit var browserStackBuild: BrowserStackBuild
  private val failedSessions = mutableListOf<DeviceSessions>()
  private val failedTests = mutableListOf<String>()

  suspend fun runFailedTests(browserStackBuild: BrowserStackBuild, shards: Int): String {
    this.browserStackBuild = browserStackBuild
    getFailedTests()
    createRetryRequestObject(shards)

    val url = URLBuilder(
      URLProtocol.HTTPS,
      hostURL,
      pathSegments = listOf(appAutomate, browserStackBuild.framework, apiVersion, build)
    ).buildString()

    val response: BuildRequest = client.post(url) {
      headers { append(HttpHeaders.ContentType, "application/json") }
      setBody(createRetryRequestObject(shards))
    }.body()

    return response.build_id ?: ""
  }

  private fun createRetryRequestObject(shards: Int): JsonElement {
    var requestObject = mapOf(
      "shards" to createShardsObject(shards),
      "app" to browserStackBuild.input_capabilities.app,
      "testSuite" to browserStackBuild.input_capabilities.testSuite,
      "testSuite" to browserStackBuild.input_capabilities.testSuite,
      "buildTag" to "${browserStackBuild.input_capabilities.buildTag}_retry",
      "project" to browserStackBuild.input_capabilities.project,
      "networkLogs" to browserStackBuild.input_capabilities.networkLogs,
      "deviceLogs" to browserStackBuild.input_capabilities.deviceLogs,
      "debugscreenshots" to browserStackBuild.input_capabilities.debugscreenshots,
      "devices" to browserStackBuild.input_capabilities.devices
    )
    return requestObject.toJsonElement()
  }

  private fun createShardsObject(shards: Int): Map<String, Any> {
    val mapping = mutableListOf<Any>()
    val chunckedBy = if (failedTests.size % shards != 0) {
      failedTests.size / shards + 1
    } else {
      failedTests.size / shards
    }
    val listOfFailedLists = failedTests.chunked(chunckedBy)

    for (i in 0 until shards) {
      val shard = mapOf(
        "name" to "Shard $i",
        "strategy" to "only-testing",
        "values" to listOfFailedLists[i]
      )
      mapping.add(shard)
    }

    return mapOf<String, Any>(
      "numberOfShards" to shards,
      "deviceSelection" to "any",
      "mapping" to mapping
    )
  }

  private suspend fun getFailedTests() {
    getFailedSessions()
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

  private fun getFailedSessions() {
    browserStackBuild.devices.forEach { device ->
      device.sessions.forEach { session ->
        if (session.status != passed) {
          failedSessions.add(session)
        }
      }
    }
  }
}
