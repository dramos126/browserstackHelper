package com.ramos.danny

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.ramos.danny.plugins.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json



fun main() {
  embeddedServer(Netty, port = 8088, host = "0.0.0.0", module = Application::module)
    .start(wait = true)
}

val client = HttpClient(CIO) {
  expectSuccess = true
  install(Auth) {
    basic { credentials { BasicAuthCredentials("", "") } }
  }
  install(ContentNegotiation) {
    json(Json {
      prettyPrint = true
      isLenient = true
      ignoreUnknownKeys = true
    })
  }
}

fun Application.module() {
  configureMonitoring()
//  configureSerialization()
  configureRouting()
}
