package ru.polenova.dto

import ru.polenova.model.PostModel
import ru.polenova.model.StatusUser
import ru.polenova.model.*
import ru.polenova.service.UserService
import java.time.format.DateTimeFormatter

class PostResponseDto(
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
    var statusUser: StatusUser = StatusUser.NORMAL,
    val idUser: Long,
    val attachmentId: String? = null
) {
    companion object {
        fun fromModel(postModel: PostModel, idUser: Long, userService: UserService): PostResponseDto {
            val pressedPostUp = postModel.upUserIdList.contains(idUser)
            val pressedPostDown = postModel.downUserIdList.contains(idUser)
            val postUpCount = postModel.upUserIdList.size
            val postDownCount = postModel.downUserIdList.size

            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z");
            val dateOfPostString = postModel.dateOfCreate?.format(formatter)

            return PostResponseDto(
                idPost = postModel.idPost,
                postText = postModel.postText,
                dateOfCreate = dateOfPostString,
                userName = postModel.userName,
                postUpCount = postUpCount,
                pressedPostUp = pressedPostUp,
                pressedPostDown = pressedPostDown,
                postDownCount = postDownCount,
                postName = postModel.postName,
                idUser = 0L,
                statusUser = StatusUser.NORMAL,
                linkForPost = postModel.linkForPost,
                attachmentId = postModel.attachment?.id
            )
        }
    }
}

