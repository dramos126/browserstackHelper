package com.ramos.danny.utils

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun Map<*, *>.toJsonObject(): JsonObject {
  val map = mutableMapOf<String, JsonElement>()
  this.forEach {
    val key = it.key as? String ?: return@forEach
    val value = it.value ?: return@forEach
    when (value) {
      // convert containers into corresponding Json containers
      is Map<*, *> -> map[key] = (value).toJsonObject()
      is List<*> -> map[key] = value.toJsonObject()
      // convert the value to a JsonPrimitive
      else -> map[key] = JsonPrimitive(value.toString())
    }
  }
  return JsonObject(map)
}