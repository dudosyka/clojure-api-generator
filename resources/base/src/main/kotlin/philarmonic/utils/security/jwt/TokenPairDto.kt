package philarmonic.utils.security.jwt


import kotlinx.serialization.Serializable

@Serializable
data class TokenPairDto(
    val accessToken: String,
    val refreshToken: String
)
