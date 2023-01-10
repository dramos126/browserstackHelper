package com.ramos.danny.browserstack

import kotlinx.serialization.Serializable

@Serializable
data class Build(
  val id: String,
  val framework: String,
  val devices: List<Devices>,
  val input_capabilities: InputCapabilities
)

@Serializable
data class Devices(
  val sessions: List<DeviceSessions>
)

@Serializable
data class DeviceSessions(
  val id: String,
  val status: String
)

@Serializable
data class InputCapabilities(
  val project: String,
  val app: String,
  val testSuite: String,
  val debugscreenshots: Boolean,
  val deviceLogs: Boolean,
  val networkLogs: Boolean,
  val buildTag: String,
  val setEnvVariables: EnvVariables,
  val devices: List<String>
)

@Serializable
data class EnvVariables(
  val IS_BROWSERSTACK: Boolean,
  val BUILD_NUMBER: String
)