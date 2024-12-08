package philarmonic.utils.kodein

import com.fleetmate.lib.exceptions.ForbiddenException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import org.kodein.di.DIAware
import org.kodein.di.instance
import philarmonic.utils.security.jwt.JwtUtil

/**
 * A [KodeinAware] base class for Controllers handling routes.
 * It allows to easily get dependencies, and offers some useful extensions.
 */
@Suppress("KDocUnresolvedReference")
abstract class KodeinController : DIAware {
    /**
     * Injected dependency with the current [Application].
     */
    val application: Application by instance()

    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    abstract fun Route.registerRoutes()

    fun ApplicationCall.getAuthorized() = JwtUtil.decodeAccessToken(principal<JWTPrincipal>() ?: throw ForbiddenException())

    fun ApplicationCall.getRefreshed() = JwtUtil.decodeRefreshToken(principal<JWTPrincipal>() ?: throw ForbiddenException())
}