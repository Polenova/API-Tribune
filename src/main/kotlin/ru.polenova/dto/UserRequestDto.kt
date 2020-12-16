package ru.polenova.dto

class UserRequestDto (
    val username: String,
    val password: String,
    val attachmentImage: String? = null
)