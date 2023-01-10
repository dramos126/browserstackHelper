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
      val buildId = call.request.queryParameters[build] ?: "".apply {
        call.respondText(
          "Missing 'build' parameter",
          status = HttpStatusCode.BadRequest
        )
      }
      val framework: String = call.request.queryParameters[framework]?.lowercase()
        ?: "".apply { call.respondText("Missing 'framework' parameter", status = HttpStatusCode.BadRequest) }

      val url = URLBuilder(
        URLProtocol.HTTPS,
        hostURL,
        pathSegments = listOf(appAutomate, framework, apiVersion, builds, buildId)
      ).buildString()

      val browserStackBuild: Build = client.get(url).body()
      call.respondText(RetryTests(browserStackBuild).getFailedTests().toString(), status = HttpStatusCode.OK)
    }
  }
}

