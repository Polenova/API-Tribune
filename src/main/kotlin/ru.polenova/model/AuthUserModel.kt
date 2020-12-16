package ru.polenova.model

import io.ktor.auth.Principal

data class AuthUserModel (
        val idUser: Long = 0,
        val username: String,
        val password: String,
        var userPostsId: MutableList<Long> = mutableListOf(),
        var attachmentImage: String? = null,
        var token: String? = null,
        var status: StatusUser = StatusUser.NORMAL,
        var readOnly: Boolean = false,
        var up: Long = 0,
        var down: Long = 0
): Principal

enum class StatusUser {
        NORMAL,
        PROMOTER,
        HATER
}