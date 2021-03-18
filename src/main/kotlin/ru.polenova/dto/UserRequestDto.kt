package ru.polenova.dto

data class UserRequestDto (
    val idUser: Long,
    val username: String,
    val password: String,
    val attachmentImage: String? = null
)