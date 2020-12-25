package ru.polenova.dto

import ru.polenova.model.StatusUser
import java.time.ZonedDateTime

data class PostRequestDto (
    val postName: String? = null,
    val postText: String? = null,
    val link: String? = null
)