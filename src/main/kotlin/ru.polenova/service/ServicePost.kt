package ru.polenova.service

import io.ktor.features.*
import io.ktor.util.*
import ru.polenova.dto.PostRequestDto
import ru.polenova.dto.PostResponseDto
import ru.polenova.exception.UserAccessException
import ru.polenova.model.AuthUserModel
import ru.polenova.model.MediaModel
import ru.polenova.model.PostModel
import ru.polenova.repository.PostRepository
import java.time.LocalDateTime

class ServicePost (private val repo: PostRepository) {

    suspend fun getAllPosts(userId: Long, userService: UserService): List<PostResponseDto> {
        return repo.getAllPosts().map { PostResponseDto.fromModel(it, userId, userService) }
    }

    @KtorExperimentalAPI
    suspend fun getRecent(userId: Long, userService: UserService): List<PostResponseDto> {
        return repo.getRecent().map { PostResponseDto.fromModel(it, userId, userService) }
    }

    @KtorExperimentalAPI
    suspend fun save(input: PostRequestDto, me: AuthUserModel, userService: UserService): PostResponseDto {
        val date = LocalDateTime.now()
        val model = PostModel(
            idPost = 0L,
            dateOfCreate = date,
            postName = input.postName,
            postText = input.postText,
            link = input.link,
            idUser = me.idUser
            /*postUpCount = 0,
            postDownCount = 0,
            pressedPostDown = false,
            pressedPostUp = false,*/
            //attachment = input.attachmentId?.let { MediaModel(id = it) }

        )
        return PostResponseDto.fromModel(repo.savePost(model), me.idUser, userService)
    }

    @KtorExperimentalAPI
    suspend fun getByIdPost(idPost: Long) = repo.getByIdPost(idPost) ?: throw NotFoundException()



    @KtorExperimentalAPI
    suspend fun getPostsAfter(idPost: Long, userId: Long, userService: UserService): List<PostResponseDto> {
        val listPostsAfter = repo.getPostsAfter(idPost) ?: throw NotFoundException()
        return listPostsAfter.map { PostResponseDto.fromModel(it, userId, userService) }
    }

    @KtorExperimentalAPI
    suspend fun getPostsBefore(idPost: Long, userId: Long, userService: UserService): List<PostResponseDto> {
        val listPostsBefore = repo.getPostsBefore(idPost) ?: throw NotFoundException()
        return listPostsBefore.map { PostResponseDto.fromModel(it, userId, userService) }
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
    suspend fun upById(idUser: Long, idPost: Long, userService: UserService): PostResponseDto {
        if (getByIdPost(idPost).upUserIdMap.contains(idUser)) {
            throw UserAccessException("You are have reaction of this post")
        }
        val post = repo.upById(idPost, idUser)?: throw NotFoundException()
        val userPost = userService.getByIdUser(post.idUser)
        val user = userService.getByIdUser(idUser)
        return PostResponseDto.fromModel(post, idUser, userService)
    }
    /*@KtorExperimentalAPI
    suspend fun disUpById(idUser: Long, idPost: Long, userService: UserService): PostResponseDto {
        val model = repo.disUpById(idPost, me.idUser) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model, idPost)
    }
*/
    @KtorExperimentalAPI
    suspend fun downById(idUser: Long, idPost: Long, userService: UserService): PostResponseDto {
        if (getByIdPost(idPost).upUserIdMap.contains(idUser)||getByIdPost(idPost).downUserIdMap.contains(idUser)) {
            throw UserAccessException("You are have reaction of this post")
        }
        val post = repo.downById(idPost, idUser)?: throw NotFoundException()
        val userPost = userService.getByIdUser(post.idUser)
        val user = userService.getByIdUser(idUser)
        return PostResponseDto.fromModel(post, idUser, userService)

    }

    /*@KtorExperimentalAPI
    suspend fun disDownById(idPost: Long, me: AuthUserModel): PostResponseDto {
        val model = repo.disDownById(idPost, me.idUser) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model, idPost)
    }*/
}