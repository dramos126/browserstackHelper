package com.ramos.danny.plugins

import com.ramos.danny.browserstack.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*

fun Application.configureRouting() {
  val browserStack = BrowserStack()


  routing {
    get("/") {
      call.respondText("Hello World!")
    }

    get("/browserstack/results") {
      val buildId = call.request.queryParameters[build] ?: "".also {
        call.respondText("Missing 'build' parameter", status = HttpStatusCode.BadRequest)
        return@get
      }

      val framework = call.request.queryParameters[framework]?.lowercase() ?: "".also {
        call.respondText("Missing 'framework' parameter", status = HttpStatusCode.BadRequest)
        return@get }

      call.response.headers.append("Content-Type", "application/json")
      call.respond(HttpStatusCode.OK, browserStack.buildResults(buildId, framework).toString())
    }

    get("/browserstack/retry") {
      val buildId = call.request.queryParameters[build] ?: "".also {
        call.respondText("Missing 'build' parameter", status = HttpStatusCode.BadRequest)
        return@get
      }

      val framework = call.request.queryParameters[framework]?.lowercase() ?: "".also {
        call.respondText("Missing 'framework' parameter", status = HttpStatusCode.BadRequest)
        return@get }

      val browserStackShards = call.request.queryParameters[testShards]?.toIntOrNull()

      val retryBuildId = browserStack.runFailedTests(buildId, framework, browserStackShards ?: 2)
      call.respond(retryBuildId.first, retryBuildId.second)
    }
  }
}

