package ru.polenova.model

import java.time.LocalDateTime
import java.time.ZonedDateTime
import kotlin.Long as Long

data class PostModel (
    val idUser: Long,
    val userName: String? = null,
    val dateOfCreate: ZonedDateTime? = null,
    val linkForPost: String? = null,
    val postName: String? = null,
    val postText: String? = null,
    val idPost: Long,
    val user: AuthUserModel? = null,
    val attachment: MediaModel? = null,
    var upUserIdMap: MutableMap<Long, LocalDateTime> = mutableMapOf(),
    var downUserIdMap: MutableMap<Long, LocalDateTime> = mutableMapOf()
)
