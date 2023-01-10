package com.ramos.danny.browserstack

import kotlinx.serialization.Serializable

@Serializable
data class Build(
    val id: String,
    val framework: String,
    val devices: List<Devices>
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