package philarmonic.modules.auth.controller


import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import philarmonic.utils.kodein.KodeinController
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import philarmonic.modules.auth.data.dto.AuthInputDto
import philarmonic.modules.auth.service.AuthService

class AuthController(override val di: DI) : KodeinController() {
    private val authService: AuthService by instance()
    override fun Route.registerRoutes() {
        route("auth") {
            post {
                val authInputDto = call.receive<AuthInputDto>()

                call.respond(authService.auth(authInputDto))
            }
        }
        authenticate("default") {
            get("current") {
                call.respond(authService.getAuthorized(call.getAuthorized()))
            }
        }
        authenticate("refresh") {
            post("refresh") {
                val refreshDto = call.getRefreshed();

                call.respond(authService.refresh(refreshDto))
            }
        }
    }

}