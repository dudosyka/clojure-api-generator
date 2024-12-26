package philarmonic

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import philarmonic.conf.AppConf
import philarmonic.modules.auth.controller.AuthController
import philarmonic.modules.auth.service.AuthService
import philarmonic.plugins.*
import philarmonic.utils.database.DatabaseConnector
import philarmonic.utils.kodein.bindSingleton
import philarmonic.utils.kodein.kodeinApplication
{{:imports-list}}

fun main() {
    embeddedServer(Netty, port = AppConf.server.port, host = AppConf.server.host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureCORS()
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureExceptionFilter()

    kodeinApplication("/") {
        // ----- Services ------
        {{:services-list}}
        bindSingleton { AuthService(it) }

        // ----- Controllers ------
        {{:controllers-list}}
        bindSingleton { AuthController(it) }
    }

    DatabaseConnector(
        {{:models-list}}
    ) {}

}
