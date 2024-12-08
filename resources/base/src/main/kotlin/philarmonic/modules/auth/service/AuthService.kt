package philarmonic.modules.auth.service


import com.fleetmate.lib.exceptions.ForbiddenException
import io.ktor.util.date.*
import philarmonic.utils.kodein.KodeinService
import org.kodein.di.DI
import philarmonic.modules.auth.data.dto.AuthInputDto
import philarmonic.modules.auth.data.model.UserLoginModel
import philarmonic.modules.user.dto.UserDto
import philarmonic.modules.user.model.UserModel
import philarmonic.utils.security.bcrypt.CryptoUtil
import philarmonic.utils.security.jwt.AuthorizedUser
import philarmonic.utils.security.jwt.JwtUtil
import philarmonic.utils.security.jwt.RefreshDto
import philarmonic.utils.security.jwt.TokenPairDto
import java.util.NoSuchElementException

class AuthService(di: DI): KodeinService(di) {
    fun auth(authInputDto: AuthInputDto): TokenPairDto {
        val user: Pair<Int, String> = try {
            UserModel.getByLogin(authInputDto.login)
        } catch (e: NoSuchElementException) {
            throw ForbiddenException()
        }
        val lastLogin = getTimeMillis()

        if (!CryptoUtil.compare(authInputDto.password, user.second) ||
            UserLoginModel.setLastLogin(user.first, lastLogin) == 0)
            throw ForbiddenException()

        return TokenPairDto(
            JwtUtil.createToken(user.first),
            JwtUtil.createToken(user.first, lastLogin)
        )
    }

    fun refresh(refreshDto: RefreshDto): TokenPairDto {
        val lastLogin = UserLoginModel.getLastLogin(refreshDto.id)

        if (lastLogin != refreshDto.lastLogin)
            throw ForbiddenException()

        if (UserLoginModel.setLastLogin(refreshDto.id, lastLogin) == 0)
            throw ForbiddenException()

        return TokenPairDto(
            JwtUtil.createToken(refreshDto.id),
            JwtUtil.createToken(refreshDto.id, lastLogin)
        )
    }

    fun getAuthorized(authorized: AuthorizedUser): UserDto {
        return UserModel.getOne(authorized.id) ?: throw ForbiddenException()
    }
}