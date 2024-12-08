package philarmonic.utils.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import philarmonic.conf.AppConf
import java.util.*

typealias EncodedRules = MutableMap<Int, MutableList<Int>>

object JwtUtil {
    fun createToken(userId: Int, lastLogin: Long? = null): String {
        return JWT.create()
            .withIssuer(AppConf.jwt.domain)
            .withIssuedAt(Date(System.currentTimeMillis()))
            .withExpiresAt(
                Date(
                    System.currentTimeMillis() +
                            (if (lastLogin != null) AppConf.jwt.refreshExpirationTime else AppConf.jwt.expirationTime) * 1000
                )
            )
            .apply {
                withClaim("id", userId)
                if (lastLogin != null)
                    withClaim("lastLogin", lastLogin)

            }.sign(Algorithm.HMAC256(AppConf.jwt.secret))
    }

    fun decodeAccessToken(principal: JWTPrincipal): AuthorizedUser = AuthorizedUser(
        id = principal.getClaim("id", Int::class)!!
    )

    fun decodeRefreshToken(principal: JWTPrincipal): RefreshDto = RefreshDto(
        id = principal.getClaim("id", Int::class)!!,
        lastLogin = principal.getClaim("lastLogin", Long::class)!!
    )
}