package com.ramos.danny.browserstack.response

import kotlinx.serialization.Serializable

@Serializable
data class BuildRequest(
  val message: String,
  val build_id: String?
)