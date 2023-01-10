package com.ramos.danny.browserstack

import com.ramos.danny.client
import io.ktor.client.call.*
import io.ktor.client.request.*

class RetryTests(private val browserStackBuild: Build) {
  private val failedSessions = mutableListOf<Sessions>()
  private val failedTests = mutableListOf<String>()

  private fun getFailedSessions() {
    browserStackBuild.devices.forEach { device ->
      device.sessions.forEach { session ->
        if (session.status != passed) {
          failedSessions.add(session)
        }
      }
    }
  }

  suspend fun getFailedTests(): MutableList<String> {
    getFailedSessions()
    failedSessions.forEach {
      val session: Session =
        client.get(hostURL + appAutomate + "${browserStackBuild.framework}/v2/" + builds + browserStackBuild.id + "/$buildSessions" + it.id)
          .body()
      session.testcases.data.forEach { data ->
        data.testcases.forEach { testCase ->
          if (testCase.status != passed) {
            failedTests.add("${data.`class`}/${testCase.name}")
          }
        }
      }
    }
    return failedTests
  }
}