package com.ramos.danny.browserstack

import com.ramos.danny.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class BrowserStack {
  lateinit var browserStackBuild: Build
  private val failedSessions = mutableListOf<DeviceSessions>()
  private val failedTests = mutableListOf<String>()

  suspend fun runFailedTests(build: Build, shards: Int? = 2) {
    browserStackBuild = build
    getFailedTests()

  }

  private fun createShardObject(shards: Int) {
    var mappings = emptyMap<String, Any>()
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