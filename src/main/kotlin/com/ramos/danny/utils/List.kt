package com.ramos.danny.utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

fun List<*>.toJsonObject(): JsonElement {
  val list = mutableListOf<JsonElement>()

  this.forEach {
    val value = it as? Any ?: return@forEach
    when (value) {
      is Map<*, *> -> list.add((value).toJsonObject())
      is List<*> -> list.add(value.toJsonObject())
      else -> list.add(JsonPrimitive(value.toString()))
    }
  }
  return JsonArray(list)
}