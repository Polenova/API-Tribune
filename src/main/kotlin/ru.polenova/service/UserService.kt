package ru.polenova.service

import io.ktor.features.*
import io.ktor.util.*
import org.springframework.security.crypto.password.PasswordEncoder
import ru.polenova.dto.AuthenticationRequestDto
import ru.polenova.dto.AuthenticationResponseDto
import ru.polenova.dto.PasswordChangeRequestDto
import ru.polenova.exception.InvalidPasswordException
import ru.polenova.exception.NullUsernameOrPasswordException
import ru.polenova.exception.PasswordChangeException
import ru.polenova.exception.UserExistsException
import ru.polenova.model.AuthUserModel
import ru.polenova.repository.UserRepository

class UserService (
    private val repo: UserRepository,
    private val tokenService: JWTTokenService,
    private val passwordEncoder: PasswordEncoder
) {
    suspend fun getModelByIdPassword(id: Long, password: String): AuthUserModel? {
        return repo.getByIdPassword(id, password)
    }

    suspend fun getByUserName(username: String): AuthUserModel? {
        return repo.getByUsername(username)
    }

    @KtorExperimentalAPI
    suspend fun changePassword(idUser: Long, input: PasswordChangeRequestDto): AuthenticationResponseDto {
        val model = repo.getByIdUser(idUser) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.old, model.password)) {
            throw PasswordChangeException("Wrong password!")
        }
        val copy = model.copy(password = passwordEncoder.encode(input.new))
        repo.save(copy)
        val token = tokenService.generate(copy)
        return AuthenticationResponseDto(token)
    }
    suspend fun save(username: String, password: String): AuthenticationResponseDto {
        if (username == "" || password == "") {
            throw NullUsernameOrPasswordException("Username or password is empty")
        } else if (repo.getByUsername(username) != null) {
            throw UserExistsException("User already exists")
        } else {
            val model = repo.save(AuthUserModel(username = username, password = password))
            val token = tokenService.generate(model)
            return AuthenticationResponseDto(token)
        }
    }
    @KtorExperimentalAPI
    suspend fun authenticate(input: AuthenticationRequestDto): AuthenticationResponseDto {
        val model = repo.getByUsername(input.username) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.password, model.password)) {
            throw InvalidPasswordException("Wrong password!")
        }

        val token = tokenService.generate(model)
        return AuthenticationResponseDto(token)
    }
}