package philarmonic.modules.user.dto


import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val login: String?,
    val fullName: String
)
