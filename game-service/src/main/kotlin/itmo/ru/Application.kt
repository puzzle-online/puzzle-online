package itmo.ru

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import itmo.ru.plugins.Session
import itmo.ru.plugins.configureRouting
import itmo.ru.puzzle.domain.model.ClientId
import kotlinx.serialization.json.Json
import java.time.Duration

fun main() {
    embeddedServer(Netty, port = 3000, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            }
        )
    }
    install(Sessions) {
        cookie<Session>("client_session")
    }
    intercept(ApplicationCallPipeline.Call) {
        if (call.sessions.get<Session>() == null) {
            call.sessions.set(Session(ClientId(generateNonce())))
        }
    }
    configureRouting()
}
