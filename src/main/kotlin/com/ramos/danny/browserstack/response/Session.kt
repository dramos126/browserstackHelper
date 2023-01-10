package com.ramos.danny.browserstack

import kotlinx.serialization.Serializable

@Serializable
data class Session(
  val testcases: TestCaseData
)

@Serializable
data class TestCaseData(
  val data: List<Data>
)

@Serializable
data class Data(
  val `class`: String,
  val testcases: List<TestCase>
)

@Serializable
data class TestCase(
  val name: String,
  val status: String
)