package com.ramos.danny.plugins

import com.ramos.danny.browserstack.*
import com.ramos.danny.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*

@Suppress("IMPLICIT_CAST_TO_ANY")
fun Application.configureRouting() {
  routing {
    get("/") {
      call.respondText("Hello World!")
    }

    get("/retry") {
      val buildId = call.request.queryParameters[build] ?: "".also {
        call.respondText("Missing 'build' parameter", status = HttpStatusCode.BadRequest)
        return@get
      }
      val framework = call.request.queryParameters[framework]?.lowercase() ?: "".also {
        call.respondText("Missing 'framework' parameter", status = HttpStatusCode.BadRequest)
        return@get }
      val browserStackThreads = call.request.queryParameters[testThreads]?.toIntOrNull()

      val url = URLBuilder(
        URLProtocol.HTTPS,
        hostURL,
        pathSegments = listOf(appAutomate, framework, apiVersion, builds, buildId)
      ).buildString()

      val build: Build = client.get(url).body()
      BrowserStack().runFailedTests(build)
//      call.respondText(BrowserStack(build).getFailedTests().toString(), status = HttpStatusCode.OK)
    }
  }
}

