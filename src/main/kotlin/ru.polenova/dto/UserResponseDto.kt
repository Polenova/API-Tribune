package ru.polenova.dto

import io.ktor.util.*
import ru.polenova.model.AuthUserModel
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
    @KtorExperimentalAPI
    companion object {
        fun fromModel(model: AuthUserModel): UserResponseDto {
            return UserResponseDto(
                idUser = model.idUser,
                username = model.username,
                attachmentImage = model.attachmentImage,
                status = model.status,
                token = model.token,
                readOnly = model.readOnly
            )
        }

    }
}
