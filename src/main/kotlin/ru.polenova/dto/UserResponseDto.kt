package ru.polenova.dto

import io.ktor.util.*
import ru.polenova.model.StatusUser
import ru.polenova.service.ServicePost
import ru.polenova.service.UserService

data class UserResponseDto(
    val idUser: Long,
    val username: String,
    val attachmentImage: String?,
    val status: StatusUser,
    val token: String?,
    val readOnly: Boolean
) {
    companion object {
        @KtorExperimentalAPI
        suspend fun fromModel(idUser: Long, userService: UserService, postService: ServicePost): UserResponseDto {
            val user = userService.getByIdUser(idUser)
            return UserResponseDto(
                idUser = user.idUser,
                username = user.username,
                attachmentImage = user.attachmentImage,
                status = userService.checkStatus(user.idUser),
                token = user.token,
                readOnly = userService.checkReadOnly(idUser, postService)
            )
        }
    }
}
