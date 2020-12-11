package ru.polenova.service

import io.ktor.features.*
import io.ktor.util.*
import ru.polenova.dto.PostRequestDto
import ru.polenova.dto.PostResponseDto
import ru.polenova.exception.UserAccessException
import ru.polenova.model.AuthUserModel
import ru.polenova.model.MediaModel
import ru.polenova.model.MediaType
import ru.polenova.model.PostModel
import ru.polenova.repository.PostRepository

class ServicePost (private val repo: PostRepository) {

    suspend fun getAllPosts(userId: Long): List<PostResponseDto> {
        return repo.getAllPosts().map { PostResponseDto.fromModel(it, userId) }
    }

    suspend fun getRecent(idPost: Long): List<PostResponseDto> {
        return repo.getRecent().map { PostResponseDto.fromModel(it, idPost) }
    }

    suspend fun save(input: PostRequestDto, me: AuthUserModel): PostResponseDto {
        val model = PostModel(
            idPost = 0L,
            postName = input.postName,
            postText = input.postText,
            linkForPost = input.linkForPost,
            dateOfCreate = input.dateOfCreate,
            user = me,
            // ??
            idUser = 0L,
            postUpCount = 0,
            postDownCount = 0,
            pressedPostDown = false,
            pressedPostUp = false,
            //
            attachment = input.attachmentId?.let { MediaModel(id = it,
                mediaType = MediaType.IMAGE) }

        )
        return PostResponseDto.fromModel(repo.savePost(model), me.idUser)
    }

    @KtorExperimentalAPI
    suspend fun saveById(idPost: Long, input: PostRequestDto, me: AuthUserModel): PostResponseDto {
        val model = PostModel(
            idPost = 0L,
            postName = input.postName,
            postText = input.postText,
            linkForPost = input.linkForPost,
            dateOfCreate = input.dateOfCreate,
            user = me,
            // ??
            idUser = 0L,
            postUpCount = 0,
            postDownCount = 0,
            pressedPostDown = false,
            pressedPostUp = false,
            //
            attachment = input.attachmentId?.let { MediaModel(id = it,
                mediaType = MediaType.IMAGE) }
        )
        val existingPostModel = repo.getByIdPost(idPost) ?: throw NotFoundException()
        if (existingPostModel.user?.idUser != me.idUser) {
            throw UserAccessException("Access denied, Another user posted this post")

        }
        return PostResponseDto.fromModel(repo.savePost(model), me.idUser)
    }

    @KtorExperimentalAPI
    suspend fun getByIdPosts(idPost: Long, userId: Long): PostResponseDto {
        val model = repo.getByIdPost(idPost) ?: throw NotFoundException()

        return PostResponseDto.fromModel(model, userId)
    }

    @KtorExperimentalAPI
    suspend fun getPostsAfter(idPost: Long, userId: Long): List<PostResponseDto> {
        val listPostsAfter = repo.getPostsAfter(idPost) ?: throw NotFoundException()
        return listPostsAfter.map { PostResponseDto.fromModel(it, userId) }
    }

    @KtorExperimentalAPI
    suspend fun getPostsBefore(idPost: Long, userId: Long): List<PostResponseDto> {
        val listPostsAfter = repo.getPostsBefore(idPost) ?: throw NotFoundException()
        return listPostsAfter.map { PostResponseDto.fromModel(it, userId) }
    }

    @KtorExperimentalAPI
    suspend fun removePostByIdPost(idPost: Long, me: AuthUserModel): Boolean {
        val model = repo.getByIdPost(idPost) ?: throw NotFoundException()
        return if (model.user == me) {
            repo.removePostByIdPost(idPost)
            true
        } else {
            false
        }
    }

    @KtorExperimentalAPI
    suspend fun upById(idPost: Long, me: AuthUserModel): PostResponseDto {
        val model = repo.upById(idPost, me.idUser) ?: throw NotFoundException()
        val userOfPost = model.user!!
        /*if (!userOfPost.firebaseToken.isNullOrEmpty()) {
            fcmService.send(userOfPost.id, userOfPost.firebaseToken, "Your post liked by ${user.username}")
        }*/
        return PostResponseDto.fromModel(model, idPost)
    }
    @KtorExperimentalAPI
    suspend fun disUpById(idPost: Long, me: AuthUserModel): PostResponseDto {
        val model = repo.disUpById(idPost, me.idUser) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model, idPost)
    }

    @KtorExperimentalAPI
    suspend fun downById(idPost: Long, me: AuthUserModel): PostResponseDto {
        val model = repo.downById(idPost, me.idUser) ?: throw NotFoundException()
        val userOfPost = model.user!!
        /*if (!userOfPost.firebaseToken.isNullOrEmpty()) {
            fcmService.send(userOfPost.id, userOfPost.firebaseToken, "Your post liked by ${user.username}")
        }*/
        return PostResponseDto.fromModel(model, idPost)
    }
    @KtorExperimentalAPI
    suspend fun disDownById(idPost: Long, me: AuthUserModel): PostResponseDto {
        val model = repo.disDownById(idPost, me.idUser) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model, idPost)
    }
}