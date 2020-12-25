package ru.polenova.dto

import io.ktor.util.*
import ru.polenova.model.PostModel
import ru.polenova.model.StatusUser
import ru.polenova.model.*
import ru.polenova.service.UserService
import java.time.format.DateTimeFormatter

data class PostResponseDto(
    val idPost: Long,
    val userName: String?,
    val postName: String? = null,
    val postText: String? = null,
    val linkForPost: String? = null,
    val dateOfCreate: String? = null,
    var postUpCount: Int,
    var postDownCount: Int,
    var pressedPostUp: Boolean,
    var pressedPostDown: Boolean,
    var statusUser: StatusUser = StatusUser.NONE,
    val idUser: Long,
    val attachmentId: String? = null
) {
    companion object {
        @KtorExperimentalAPI
        suspend fun fromModel(postModel: PostModel, idUser: Long, userService: UserService): PostResponseDto {
            val pressedPostUp = postModel.upUserIdMap.contains(idUser)
            val pressedPostDown = postModel.downUserIdMap.contains(idUser)
            val postUpCount = postModel.upUserIdMap.size
            val postDownCount = postModel.downUserIdMap.size
            val user = userService.getByIdUser(idUser)
            val postUser = userService.getByUserName(postModel.userName.toString())
            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z");
            val dateOfPostString = postModel.dateOfCreate?.format(formatter)

            return PostResponseDto(
                idPost = postModel.idPost,
                postText = postModel.postText,
                dateOfCreate = dateOfPostString,
                userName = postUser.toString(),
                postUpCount = postUpCount,
                pressedPostUp = pressedPostUp,
                pressedPostDown = pressedPostDown,
                postDownCount = postDownCount,
                postName = postModel.postName,
                idUser = user.idUser,
                statusUser = StatusUser.NONE,
                linkForPost = postModel.linkForPost,
                attachmentId = postModel.attachment?.id
            )
        }
    }
}

