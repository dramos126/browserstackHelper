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
    val sessions: List<Sessions>
)

@Serializable
data class Sessions(
    val id: String,
    val status: String
)